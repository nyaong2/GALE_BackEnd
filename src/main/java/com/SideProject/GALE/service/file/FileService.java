package com.SideProject.GALE.service.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.components.io.utils.FileUtils;
import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.exception.CustomRuntimeException_Msg;
import com.SideProject.GALE.mapper.file.FileMapper;
import com.SideProject.GALE.model.file.FileDto;
import com.SideProject.GALE.model.file.FileReviewDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
	//https://gaemi606.tistory.com/entry/Spring-Boot-REST-API-%ED%8C%8C%EC%9D%BC-%EC%97%85%EB%A1%9C%EB%93%9C-%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C
	//private final Path dirLocation;
	private final FileUtils fileUtils;
	private final FileMapper fileMapper;
	
	@Value("${spring.servlet.multipart.location}") //Value로 주입받는 매개변수는 그 자체로 final 값으로 간주함.
	private String basicFolderLocation; // Path : [C:\GALE]
	
	private final String parentFolderName_Board = "Board";
	private final String parentFolderName_Board_Review = "Review";
	
	private final List<String> allowImageExtension = Arrays.asList(
			"jpg", "jpeg", "png", "bmp");
	
	
	@Transactional(propagation = Propagation.NESTED)
	public void Upload_BoardImages(int board_Number, List<MultipartFile> imageFileList) // Board
	{
		int image_Order = 1;
		String exceptionSaveFolderPath = null;  // Exception 발생 시 폴더 생성 rollback을 위해 값 주기. [Exception에서 Try문안에 선언된 변수 접근 불가하기에 밖으로 빼서 설정 후 Exception 발생 시 이 폴더를 참조하여 그 폴더를 지울 수 있도록.]
		List<FileDto> dbSaveListFileDto = new ArrayList<>(); // dbSaveListFileDto -> 추 후 for문이 끝나면, 이 리스트를 토대로 한번에 sql자체에서 for문으로 다 등록하도록. (sql 작업 최소화)

			
		// 1. 카테고리+idx 폴더 생성
		File saveFolder = new File(String.format("%s\\%s\\%s", basicFolderLocation, parentFolderName_Board, board_Number)); // 파일 저장폴더위치 + 유형 + 카테고리 + 게시물 번호
		exceptionSaveFolderPath = saveFolder.getPath(); // Exception 발생 시 폴더 생성 rollback을 위해 값 주기. [Exception에서 Try문안에 선언된 변수 접근 불가.]
		
		if (saveFolder.exists() == false)
			saveFolder.mkdirs();
		else
		{
			this.Remove_BoardImagesFolder(board_Number);
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		try { // 폴더 삭제를 위해 try 필요함.
			for (MultipartFile multipartFile : imageFileList) 
			{
				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				String fileExtension = FilenameUtils.getExtension(fileName);
				
				// 2. 허용된 확장자가 없으면 throw
				if (allowImageExtension.contains(fileExtension) == false) 
					throw new CustomRuntimeException_Msg(ResCode.BAD_REQUEST_FILE_NOTALLOW_EXTENSION, String.format("[{}]은 지원하지 않는 확장자입니다.", fileName));


				// 3. 이미지 로컬에 저장
				multipartFile.transferTo(Paths.get(saveFolder.getPath() + "\\" + fileName)); //2번에서 설정한 폴더 path 위치 + 파일명


				// 4. 파일 해쉬 저장
				String hashFileName = fileUtils.ExtractFileHashSHA256(saveFolder.getPath() + "\\" + fileName);
				hashFileName = hashFileName + "." + fileExtension; //암호화 + 파일확장자


				// 5. 파일 이름 변경을 위한 path 생성
				Path originalFilePath = Paths.get(saveFolder.getPath() + "\\" + fileName);
				Path hashFilePath = Paths.get(saveFolder.getPath() + "\\" + hashFileName);

				
				// 6. 오리지날 이름 -> 해시파일 이름 변경
				Files.move(originalFilePath,hashFilePath);
				

				// 7. 파일 DB 데이터 추가
				FileDto fileDto = new FileDto();
				fileDto.setBoard_number(board_Number);
				fileDto.setOrigin_file_name(fileName);
				fileDto.setStored_file_name(hashFileName);
				fileDto.setOrder_number(image_Order);
					
				dbSaveListFileDto.add(fileDto);
					
				image_Order++; // 이미지 카운터 증가
			}// foreach 문 End
			
			
			// 8. DB Foreach로 모두 파일저장된 dto 등록
			if(fileMapper.Upload_Board(dbSaveListFileDto) < dbSaveListFileDto.size()) // saveFileDto -> 추 후 for문이 끝나면, 이 리스트를 토대로 한번에 sql문으로 다 등록하도록. (sql 작업 최소화)
				throw new Exception();
			
		} catch (CustomRuntimeException ex) {
			this.Remove_BoardImagesFolder(board_Number);
			throw ex;
		}
		catch (Exception ex) {
			this.Remove_BoardImagesFolder(board_Number);
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	public boolean Remove_BoardImagesFolder(int board_number) 
	{
		String saveBoardPath = String.format("%s\\%s\\%s", basicFolderLocation, parentFolderName_Board, board_number);
		return fileUtils.ForceFolderDelete(saveBoardPath);
	}
	
	
	
	@Transactional(propagation = Propagation.NESTED)
	public void Upload_ReivewImages(int board_Review_Number, List<MultipartFile> listFile)
	{
		// board_review_idx -> 리뷰게시판의 고유(PK) idx만 가지고 그 idx만 등록한 뒤 나중에 리뷰게시판의 사진 다운로드를 요청할 때 고유(PK) idx만 보내서 파일을 다운받을 수 있도록 할 것임.

		int image_Order = 1;
		ArrayList<FileReviewDto> dbSaveListFileDto = new ArrayList<>();// dbSaveListFileDto -> 추 후 for문이 끝나면, 이 리스트를 토대로 한번에 sql자체에서 for문으로 다 등록하도록. (sql 작업 최소화)

		
		// 1. 카테고리+idx 폴더 생성
		File saveFolder = new File(String.format("%s\\%s\\%s", basicFolderLocation, parentFolderName_Board_Review, board_Review_Number)); // 파일 저장폴더위치 + 유형 + Board_Review PK idx

		if (saveFolder.exists() == false)
			saveFolder.mkdirs();
		else
		{
			this.Remove_BoardReviewImagesFolder(board_Review_Number);
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		
		try 
		{ // 폴더 삭제를 위해 try 필요함.
			for (MultipartFile multipartFile : listFile) 
			{
				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				String fileExtension = FilenameUtils.getExtension(fileName);

				// 2. 허용된 확장자가 없으면 throw
				if (allowImageExtension.contains(fileExtension) == false) 
					throw new CustomRuntimeException_Msg(ResCode.BAD_REQUEST_FILE_NOTALLOW_EXTENSION, String.format("[{}]은 지원하지 않는 확장자입니다.", fileName));
				

				// 3. 이미지 로컬에 저장
				multipartFile.transferTo(Paths.get(saveFolder.getPath() + "\\" + fileName)); //2번에서 설정한 폴더 path 위치 + 파일명


				// 4. 파일 해쉬 저장
				String hashFileName =  fileUtils.ExtractFileHashSHA256(saveFolder.getPath() + "\\" + fileName);
				hashFileName = hashFileName + "." + fileExtension; //암호화 + 파일확장자


				// 5. 파일 이름 변경을 위한 path 생성
				Path originalFilePath = Paths.get(saveFolder.getPath() + "\\" + fileName);
				Path hashFilePath = Paths.get(saveFolder.getPath() + "\\" + hashFileName);

				// 6. 오리지날 이름 -> 해시파일 이름 변경
				Files.move(originalFilePath,hashFilePath);


				// 7. 파일 DB 데이터 추가
				FileReviewDto fileReviewDto = new FileReviewDto();
				fileReviewDto.setBoard_review_number(board_Review_Number);
				fileReviewDto.setOrigin_file_name(fileName);
				fileReviewDto.setStored_file_name(hashFileName); // 암호화명 + 확장자명
				fileReviewDto.setOrder_number(image_Order);

				dbSaveListFileDto.add(fileReviewDto);

				image_Order++; // 이미지 카운터 증가

			} //foreach문 End
			
			// 8. DB Foreach로 모두 파일저장된 dto 등록
			if(fileMapper.Upload_Board_Review(dbSaveListFileDto) < dbSaveListFileDto.size()) // saveFileDto -> 추 후 for문이 끝나면, 이 리스트를 토대로 한번에 sql문으로 다 등록하도록. (sql 작업 최소화)
				throw new Exception();

		} catch (CustomRuntimeException ex) {
			this.Remove_BoardReviewImagesFolder(board_Review_Number);
			throw ex;
		} catch (Exception ex) {
			this.Remove_BoardReviewImagesFolder(board_Review_Number);
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	public boolean Remove_BoardReviewImagesFolder(int board_review_number) 
	{
		String saveBoardReviewPath = String.format("%s\\%s\\%s", basicFolderLocation, parentFolderName_Board_Review, board_review_number);
		return fileUtils.ForceFolderDelete(saveBoardReviewPath);
	}
	
//	public boolean SaveProfile(MultipartFile profile) throws Exception
//	{
//		String fileName = StringUtils.cleanPath(profile.getOriginalFilename());
//		String fileExtension = fileName.substring(fileName.lastIndexOf("."));
//		
//		// Support Extension Checked
//		boolean allowExtensionCheck = false;
//		for(String ext : allowImageExtension)
//		{
//			if(fileExtension.toLowerCase().contains(ext))
//				allowExtensionCheck=true;
//		}
//		
//		if(!allowExtensionCheck)
//			throw new DenyFileExtensionException("지원하지 않는 확장자입니다. ['" + fileExtension + "']");
//		
//		
//		try {
//			String hashFileName = this.ExtractFileHashSHA256(saveFolderLocation + "\\"+ fileName);
//
//			profile.transferTo(Paths.get(saveFolderLocation + "\\" + hashFileName));
//			
//			
//		} catch (Exception e) {
//			System.out.println(e);
//			throw new FileUploadException( "['" + fileName + "'] 파일을 업로드 하지 못했습니다. 다시 시도 해주세요.");
//		}
//		
//		
//		Path oldPath = Paths.get(saveFolderLocation + "\\" + fileName);
//		Path hashPath = Paths.get(saveFolderLocation + "\\" + hashFileName + fileExtension);
//		
//		File file = new File(hashPath.toUri());
//		if(file.exists())
//		{
//			File tempFile = new File(oldPath.toUri());
//			tempFile.delete();
//			throw new FileUploadDuplicateFileException( "['" + fileName + "'] 파일은 이미 업로드 되어있습니다.");	
//		}
//		
//		Files.move( oldPath, hashPath);
//		
//		return true;
//	}
//	
//	
//	public boolean SaveProfile(MultipartFile profile) throws Exception
//	{
//		String fileName = StringUtils.cleanPath(profile.getOriginalFilename());
//		String fileExtension = fileName.substring(fileName.lastIndexOf("."));
//		
//		// Support Extension Checked
//		boolean allowExtensionCheck = false;
//		for(String ext : allowImageExtension)
//		{
//			if(fileExtension.toLowerCase().contains(ext))
//				allowExtensionCheck=true;
//		}
//		
//		if(!allowExtensionCheck)
//			throw new DenyFileExtensionException("지원하지 않는 확장자입니다. ['" + fileExtension + "']");
//		
//		
//		try {
//			String hashFileName = this.ExtractFileHashSHA256(saveFolderLocation + "\\"+ fileName);
//
//			profile.transferTo(Paths.get(saveFolderLocation + "\\" + hashFileName));
//			
//			
//		} catch (Exception e) {
//			System.out.println(e);
//			throw new FileUploadException( "['" + fileName + "'] 파일을 업로드 하지 못했습니다. 다시 시도 해주세요.");
//		}
//		
//		
//		Path oldPath = Paths.get(saveFolderLocation + "\\" + fileName);
//		Path hashPath = Paths.get(saveFolderLocation + "\\" + hashFileName + fileExtension);
//		
//		File file = new File(hashPath.toUri());
//		if(file.exists())
//		{
//			File tempFile = new File(oldPath.toUri());
//			tempFile.delete();
//			throw new FileUploadDuplicateFileException( "['" + fileName + "'] 파일은 이미 업로드 되어있습니다.");	
//		}
//		
//		Files.move( oldPath, hashPath);
//		
//		return true;
//	}
		
	@SuppressWarnings("unused")
	public File Download_BoardImage(int board_Number, int order_Number)
	{		
		File storedFile = null;
		try {
			// 1. 정보 가져오기
			FileDto dbQueryDto = new FileDto();
			dbQueryDto.setBoard_number(board_Number);
			dbQueryDto.setOrder_number(order_Number);
			dbQueryDto = fileMapper.Download_Board_ImageData(dbQueryDto);
			
			if(dbQueryDto == null)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_FILE_BOARD);
			if(StringUtils.hasText(dbQueryDto.getStored_file_name()) == false)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_FILE_BOARD);
		
			String saveFilePath = String.format("%s\\%s\\%s\\%s", // 파일 저장폴더위치 + 유형 +  (PK) Number + Stored FileName
					basicFolderLocation, parentFolderName_Board,  dbQueryDto.getBoard_number(), dbQueryDto.getStored_file_name()); 

			// 2. 파일 가져오기
			storedFile = new File(saveFilePath);
			
			if(storedFile == null)
				throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
			
		}  catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		return storedFile;
	}
	
	
	@SuppressWarnings("unused")
	public File Download_BoardReviewImage(int board_Review_Number, int order_Number)
	{		
		File storedFile = null;
		
		try 
		{
			// 1. 정보 가져오기
			FileReviewDto dbQueryDto = new FileReviewDto();
			dbQueryDto.setBoard_review_number(board_Review_Number);
			dbQueryDto.setOrder_number(order_Number);
			dbQueryDto = fileMapper.Download_Board_Review_ImageData(dbQueryDto);
			
			if(dbQueryDto == null)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_FILE_BOARDREVIEW);
			if(StringUtils.hasText(dbQueryDto.getStored_file_name()) == false)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_FILE_BOARDREVIEW);
			
			String saveFilePath = String.format("%s\\%s\\%s\\%s", // 파일 저장폴더위치 + 유형 + Category +  (PK) Number + Stored FileName
					basicFolderLocation, parentFolderName_Board_Review, dbQueryDto.getBoard_review_number(), dbQueryDto.getStored_file_name()); 

			// 2. 파일 가져오기
			storedFile = new File(saveFilePath);
			
			if(storedFile.exists() == false)
				throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);

		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		return storedFile;
	}
}
