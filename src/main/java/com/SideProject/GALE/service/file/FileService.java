package com.SideProject.GALE.service.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.components.io.utils.FileUtils;
import com.SideProject.GALE.components.io.utils.TimeUtils;
import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.exception.CustomRuntimeException_Msg;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.file.FileMapper;
import com.SideProject.GALE.model.file.FileDto;
import com.SideProject.GALE.model.file.FileUserProfileDto;
import com.SideProject.GALE.model.file.FileReviewDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
	//https://gaemi606.tistory.com/entry/Spring-Boot-REST-API-%ED%8C%8C%EC%9D%BC-%EC%97%85%EB%A1%9C%EB%93%9C-%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C
	//private final Path dirLocation;
	private final FileUtils fileUtils;
	private final FileMapper fileMapper;
	private final JwtProvider jwtProvider;
	
	@Value("${spring.servlet.multipart.location}") //Value로 주입받는 매개변수는 그 자체로 final 값으로 간주함.
	private String basicFolderLocation; // Path : [C:\GALE]
	
	private final String parentFolderName_Board = "board";
	private final String parentFolderName_Board_Review = "review";
	private final String parentFolderName_UserProfile = "user";
	
	private final List<String> allowImageExtension = Arrays.asList(
			"jpg", "jpeg", "png", "bmp");
	
	@Value("${server.fixed-ip}")
	private String serverIp;
	@Value("${server.port}")
	private String serverPort;
	
	
	public String CreateUserProfileImageNameUrl(String imageProfileName)
	{
		if(StringUtils.hasText(imageProfileName) == false)
			return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("http://")
			.append(serverIp)
			.append(":")
			.append(serverPort)
			.append("/file/download/")
			.append("user/profile?")
			.append("fileName=")
			.append(imageProfileName);
		
		return sb.toString();
	}
	
	
	public String CreateArrayBoardImageFileString(int board_Number, String arrayImageFileName)
	{
		if(StringUtils.hasText(arrayImageFileName) == false)
			return null;
		
		String[] taskImageFileNameString = arrayImageFileName.split(",");
		
		StringBuilder resultStr = new StringBuilder();
		
		for(int idx = 0; idx < taskImageFileNameString.length ; idx++)
		{
			
			// 값이 String으로 null이라면 null값 추가해서 프론트에서 null값인 경우 작업하지 않도록 처리.
			// (sql에 String으로 null 준 이유는 list에서 값이 없으면 key값이 추가가 안되는 경우가 있어서 String으로 null 반환처리함.)
			if(taskImageFileNameString[idx].equals("null"))
			{
				resultStr.append("null");			
				continue;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("http://")
				.append(serverIp)
				.append(":")
				.append(serverPort)
				.append("/file/download/")
				.append(parentFolderName_Board)
				.append("?board_Number=")
				.append(board_Number)
				.append("&fileName=")
				.append(taskImageFileNameString[idx]);
			taskImageFileNameString[idx] = sb.toString();
			
			if(idx > 0)
				resultStr.append(", ");
			
			resultStr.append(sb.toString());			
		}
		
		return resultStr.toString();
	}
	
	public String CreateArrayBoardReviewImageFileString(int board_Review_Number, String arrayImageFileName)
	{
		if(StringUtils.hasText(arrayImageFileName) == false)
			return null;
		
		String[] taskImageFileNameString = arrayImageFileName.split(",");
		
		for(int idx = 0; idx < taskImageFileNameString.length ; idx++)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("http://")
				.append(serverIp)
				.append(":")
				.append(serverPort)
				.append("/file/download/")
				.append(parentFolderName_Board)
				.append("/")
				.append(parentFolderName_Board_Review)
				.append("?board_Review_Number=")
				.append(board_Review_Number)
				.append("&fileName=")
				.append(taskImageFileNameString[idx]);
			taskImageFileNameString[idx] = sb.toString();
		}
		
		return Arrays.toString(taskImageFileNameString);
	}
	
	
	public boolean DisallowExtensionCheck(String fileExtension)
	{
		return 	(allowImageExtension.contains(fileExtension) == true) ? true : false; 
	}
	
	
	
	// -------------------------------------------------------------------------------------------
	
	
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
				if (this.DisallowExtensionCheck(fileExtension) == false) 
					throw new CustomRuntimeException_Msg(ResCode.BAD_REQUEST_FILE_NOTALLOW_EXTENSION, String.format("[{}]은 지원하지 않는 확장자입니다.", fileExtension));


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
		return fileUtils.ForceDeleteFolder(saveBoardPath);
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
				if (this.DisallowExtensionCheck(fileExtension) == false) 
					throw new CustomRuntimeException_Msg(ResCode.BAD_REQUEST_FILE_NOTALLOW_EXTENSION, String.format("[{}]은 지원하지 않는 확장자입니다.", fileExtension));
				

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
		return fileUtils.ForceDeleteFolder(saveBoardReviewPath);
	}
	
	
	public boolean Remove_ProfileImageFile(String profileImageFile)
	{
		return fileUtils.DeleteFile(profileImageFile);
	}
	
	
	
	@Transactional(propagation = Propagation.NESTED)
	public void Update_ProfileImage(HttpServletRequest request, @RequestPart MultipartFile profileImageFile)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		long currentTimeLong = TimeUtils.GetCurrentSeconds();
		String fileName = StringUtils.cleanPath(profileImageFile.getOriginalFilename());
		String fileExtension = FilenameUtils.getExtension(fileName);
		
		String exceptionOnlyProfileImagePath = null;
		
		try {
			// 1. 프로필사진 폴더 체크
			File saveFolder = new File(String.format("%s\\%s", basicFolderLocation, parentFolderName_UserProfile)); 
			if (saveFolder.exists() == false)
				saveFolder.mkdirs();
			
			// 2. 허용된 확장자가 없으면 throw
			if (this.DisallowExtensionCheck(fileExtension) == false) 
				throw new CustomRuntimeException_Msg(ResCode.BAD_REQUEST_FILE_NOTALLOW_EXTENSION, String.format("[{}]은 지원하지 않는 확장자입니다.", fileExtension));
			
			// 3. 파일명에 시간 붙일 long형태 만들기
			String currentTimeString = TimeUtils.CurrentTime_ProfileFileNameString(currentTimeLong); 

			// 4. 파일 저장 및 파일암호화
			profileImageFile.transferTo(Paths.get(saveFolder + "\\" + fileName));
			String hashFileName =  fileUtils.ExtractFileHashSHA256(saveFolder.getPath() + "\\" + fileName);
			hashFileName = currentTimeString + "_" + hashFileName + "." + fileExtension; // 등록한시간 + 암호화 + 파일확장자
			
			exceptionOnlyProfileImagePath = saveFolder.getPath() + "\\" + fileName; //익셉션 발생 시 제거하기 위해 설정. (아래 5번에서 발생시 제거돼야 함.)
			
			// 5. 이미 등록됐는지 확인 후 그것에 따라 로직 진행 (8번) 미리 5번에서 하는 이유는 7번이 진행되면 기존에 저장됐던 데이터가 덮어씌워지기 때문에 미리 백업.
			FileUserProfileDto profileDto = fileMapper.Read_UserProfile(userid);
			String backupBeforeStroedProfileName = (profileDto != null) ? profileDto.getStored_filename() : null;
			
			// 6. 파일 암호화명으로 변경
			Path originalFilePath = Paths.get(saveFolder.getPath() + "\\" + fileName);
			Path hashFilePath = Paths.get(saveFolder.getPath() + "\\" + hashFileName);

			Files.move(originalFilePath,hashFilePath);
			exceptionOnlyProfileImagePath = saveFolder.getPath() + "\\" + hashFileName; //익셉션 발생 시 제거하기 위해 설정.
			
			// 7. db에 저장
			FileUserProfileDto fileProfileDto = new FileUserProfileDto();
			fileProfileDto.setUserid(userid);
			fileProfileDto.setFilename(fileName);
			fileProfileDto.setStored_filename(hashFileName);
			fileProfileDto.setRegdate(new Date(currentTimeLong));
			
			Integer saveProfileResult = fileMapper.Save_UserProfileImage(fileProfileDto);
			if( (saveProfileResult == 1 || saveProfileResult == 2) == false ) // [값이 없는 상태에서 추가 = 1] [값이 있는 상태에서 업데이트 = 2]
				throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
			
			// 8. 이미 프로필이미지가 있었던 경우 이미 저장됐었던 전의 이미지 프로필 사진 제거
			if(StringUtils.hasText(backupBeforeStroedProfileName)) // true = 값이 있는경우
			{
				File beforeImageFile = new File( String.format("%s\\%s\\%s", basicFolderLocation, parentFolderName_UserProfile, backupBeforeStroedProfileName) );
				if(beforeImageFile.exists()) //파일이 있을 경우 제거
					beforeImageFile.delete();
			}
				
			
		} catch (CustomRuntimeException_Msg ex) {
			this.Remove_ProfileImageFile(exceptionOnlyProfileImagePath);
			throw ex;
		} catch (CustomRuntimeException ex) {
			this.Remove_ProfileImageFile(exceptionOnlyProfileImagePath);
			throw ex;
		} catch (Exception ex) {
			this.Remove_ProfileImageFile(exceptionOnlyProfileImagePath);
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	@Transactional(propagation = Propagation.NESTED)
	public void Delete_ProfileImage(HttpServletRequest request)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		FileUserProfileDto profileDto;
		try {
			profileDto = fileMapper.Read_UserProfile(userid);
			
			if(profileDto == null)
				throw new Exception();
			
			if(fileMapper.Delete_UserProfileImage(userid) != 1)
				throw new Exception();
			
			File saveFolder = new File( String.format("%s\\%s\\%s", basicFolderLocation, parentFolderName_UserProfile, profileDto.getStored_filename()) );
			if(saveFolder.exists())
				this.Remove_ProfileImageFile(saveFolder.toString());
			
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	// -----------------------------------------------------------------------------------------------
		
	
	
	@SuppressWarnings("unused")
	public File Download_BoardImage(int board_Number, String fileName)
	{
		File storedFile = null;
		
		String saveFilePath = String.format("%s\\%s\\%s\\%s", // 파일 저장폴더위치 + 유형 + Category +  (PK) Number + Stored FileName
				basicFolderLocation, parentFolderName_Board, board_Number, fileName); 

		try 
		{
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
	
	@SuppressWarnings("unused")
	public File Download_BoardReviewImage(int board_Review_Number, String fileName)
	{		
		File storedFile = null;
		
		String saveFilePath = String.format("%s\\%s\\%s\\%s", // 파일 저장폴더위치 + 유형 + Category +  (PK) Number + Stored FileName
				basicFolderLocation, parentFolderName_Board_Review, board_Review_Number, fileName); 

		try 
		{
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
	
	public File Download_UserProfileImage(String fileName)
	{
		File storedFile = null;
		
		String saveFilePath = String.format("%s\\%s\\%s", // 파일 저장폴더위치 + 유형 + Category +  (PK) Number + Stored FileName
				basicFolderLocation, parentFolderName_UserProfile, fileName); 

		try 
		{
			// 2. 파일 가져오기
			storedFile = new File(saveFilePath);
			
			if(storedFile.exists() == false)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_FILE_USERPROFILEIMAGE);

		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		return storedFile;
	}
}
