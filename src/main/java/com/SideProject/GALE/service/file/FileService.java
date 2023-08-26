package com.SideProject.GALE.service.file;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.exception.file.DenyFileExtensionException;
import com.SideProject.GALE.exception.file.FileUploadDuplicateException;
import com.SideProject.GALE.exception.file.FileUploadException;
import com.SideProject.GALE.mapper.file.FileMapper;
import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.board.BoardReviewDto;
import com.SideProject.GALE.model.file.FileDto;
import com.SideProject.GALE.model.file.FileReviewDto;
import com.SideProject.GALE.util.FileUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
	//https://gaemi606.tistory.com/entry/Spring-Boot-REST-API-%ED%8C%8C%EC%9D%BC-%EC%97%85%EB%A1%9C%EB%93%9C-%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C
	//private final Path dirLocation;
	private final FileUtils fileUtils;
	private final FileMapper fileMapper;
	
	@Value("${spring.servlet.multipart.location}") //Value로 주입받는 매개변수는 그 자체로 final 값으로 간주함.
	private String basicFolderLocation;
	
	private final String parentFolderName_Board = "board";
	private final String parentFolderName_Board_Review = "Review";
	
	private final List<String> allowImageExtension = Arrays.asList(
			"jpg", "jpeg", "png", "bmp");
	
	public boolean Save(MultipartFile multiPartFile) throws Exception
	{
		String fileName = StringUtils.cleanPath(multiPartFile.getOriginalFilename());
		String fileExtension = FilenameUtils.getExtension(fileName);
		
		// 1. 허용된 확장자가 없으면 throw
		if (allowImageExtension.contains(fileExtension) == false) 
			throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.File.FAIL_DENYFILEEXTENSION, "[\"" + fileExtension + "\"] 확장자는 지원하지 않습니다.");

		
		try {
			multiPartFile.transferTo(Paths.get(basicFolderLocation + "\\" + fileName));
		} catch (Exception e) {
			System.out.println(e);
			throw new FileUploadException( "['" + fileName + "'] 파일을 업로드 하지 못했습니다. 다시 시도 해주세요.");
		}
		
		// 2. Get File Hash
		String hashFileString = fileUtils.ExtractFileHashSHA256(basicFolderLocation + "\\"+ fileName);
		
		
		Path oldPath = Paths.get(basicFolderLocation + "\\" + fileName);
		Path hashPath = Paths.get(basicFolderLocation + "\\" + hashFileString + fileExtension);
		
		// 3. Duplication File Hash Check 
		File file = new File(hashPath.toUri());
		if(file.exists())
		{
			File tempFile = new File(oldPath.toUri());
			tempFile.delete();
			throw new FileUploadDuplicateException( "['" + fileName + "'] 파일은 이미 업로드 되어있습니다.");
		}
		
		// 4. Change Original Filename -> Hash Filename
		Files.move( oldPath, hashPath);
		
		return true;
	}
	
	
	@Transactional(propagation = Propagation.NESTED)
	public void ImageSave(int idx, int category, List<MultipartFile> listFile) // Board
	{
		Integer image_Order = null;
		String saveFolderPath = null;  // Exception 발생 시 폴더 생성 rollback을 위해 값 주기. [Exception에서 Try문안에 선언된 변수 접근 불가.]
		
		// Dto Data Board [idx,category] 설정. 추 후에 이미지 DB 저장시 필요. (여기에 미리 선언한 이유는 0번 체크를 위해.)
		FileDto fileDto = new FileDto();
		fileDto.setBoard_idx(idx);
		fileDto.setBoard_category(category);

		// 0. 글쓰기가 완료된 게시물 idx를 통해 이미지DB의 순서 가져오기. null값이 나와야 함.
		try 
		{
			image_Order = fileMapper.GetBoardImagesMaxOrder(fileDto);
			if (image_Order != null)
				throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		} catch (Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		} finally {
			image_Order = 1; // 파일 이미지 순서 1로 셋팅	
		}	
	
		
		try {
			for (MultipartFile multipartFile : listFile) 
			{
				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				String fileExtension = FilenameUtils.getExtension(fileName);

				if (multipartFile.getSize() < 1 || StringUtils.hasText(fileName) == false ) // 1. request key는 보냈지만, 실질적으로 데이터를 보내지 않은 경우 [사이즈가 1보다 작거나 파일명이 없음.]
					throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.File.FAIL_NOTFOUND, "이미지가 전송되지 않았거나 잘못된 요청입니다.");
				if (allowImageExtension.contains(fileExtension) == false) // 1-1. 허용된 확장자가 없으면 throw
					throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.File.FAIL_DENYFILEEXTENSION, "[\"" + fileExtension + "\"] 확장자는 지원하지 않습니다.");
				
				// 2. 카테고리+idx 폴더 생성
				File saveFolder = new File(basicFolderLocation + "\\" + parentFolderName_Board + "\\"+ category + "\\" + idx);
				saveFolderPath = saveFolder.getPath(); // Exception 발생 시 폴더 생성 rollback을 위해 값 주기. [Exception에서 Try문안에 선언된 변수 접근 불가.]
				if (saveFolder.exists() == false)
					saveFolder.mkdirs();

				try {
					// 3. 이미지 로컬에 저장
					multipartFile.transferTo(Paths.get(saveFolderPath + "\\" + fileName));
				} catch (Exception e) {
					throw new FileUploadException("['" + fileName + "'] 파일을 업로드 하지 못했습니다. 다시 시도 해주세요.");
				}

				// 4. 파일 해쉬 저장
				String hashFileString = null;
				try {
					hashFileString = fileUtils.ExtractFileHashSHA256(saveFolder.getPath() + "\\" + fileName);
				} catch (Exception ex) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - File");
				}

				// 5. 파일 이름 변경을 위한 path 생성
				Path originalFilePath = Paths.get(saveFolderPath + "\\" + fileName);
				Path hashFilePath = Paths.get(saveFolderPath + "\\" + hashFileString + "." + fileExtension);

				// 6. 예기치않은 문제로 이미 저장된 해쉬파일이 있는지 확인
				try {
					File file = new File(hashFilePath.toUri());
					if (file.exists()) 
					{
						File tempOldFile = new File(originalFilePath.toUri());
						tempOldFile.delete();
						throw new CustomRuntimeException(HttpStatus.CONFLICT, ResponseStatusCodeMsg.File.FAIL_DUPLICATEFILE, "['" + fileName + "'] 파일은 이미 업로드 되어있습니다.");
					}
				} catch (CustomRuntimeException ex) {
					throw ex;
				}catch (Exception ex) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - File");
				}

				// 7. 오리지날 이름 -> 해시파일 이름 변경
				try {
					Files.move(originalFilePath,hashFilePath);
				} catch (IOException e) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - File");
				}

				
				// 8. 파일 DB 저장
				try 
				{
					fileDto.setOrigin_file_name(fileName);
					fileDto.setStored_file_name(hashFileString);
					fileDto.setOrder(image_Order);
					
					Integer saveResult = fileMapper.Save(fileDto);
					if (saveResult == null) // [Fail == Save Failed]
						throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");

					image_Order++; // 이미지 카운터 증가
				} catch (CustomRuntimeException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
				}
			}
		} catch (CustomRuntimeException ex) {
			fileUtils.ForceFolderDelete(saveFolderPath);
			throw ex;
		}
		catch (Exception ex) {
			fileUtils.ForceFolderDelete(saveFolderPath);
			throw ex;
		}
	}
	
	
	@Transactional(propagation = Propagation.NESTED)
	public void ImageSave(int reviewIdx, List<MultipartFile> listFile) //Review
	{
		Integer image_Order = null;
		String saveFolderPath = null;  // Exception 발생 시 폴더 생성 rollback을 위해 값 주기. [Exception에서 Try문안에 선언된 변수 접근 불가.]
		
		// 0. 글쓰기가 완료된 게시물 idx를 통해 이미지DB의 순서 가져오기. null값이 나와야 함.
		try 
		{
			image_Order = fileMapper.GetBoardReviewImagesMaxOrder(reviewIdx);
			if (image_Order != null)
				throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		} catch (Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		} finally {
			image_Order = 1; // 파일 이미지 순서 1로 셋팅	
		}	
	
		
		try {
			for (MultipartFile multipartFile : listFile) 
			{
				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				String fileExtension = FilenameUtils.getExtension(fileName);

				if (multipartFile.getSize() < 1 || StringUtils.hasText(fileName) == false ) // 1. request key는 보냈지만, 실질적으로 데이터를 보내지 않은 경우 [사이즈가 1보다 작거나 파일명이 없음.]
					throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.File.FAIL_NOTFOUND, "이미지가 전송되지 않았거나 잘못된 요청입니다.");
				if (allowImageExtension.contains(fileExtension) == false) // 1-1. 허용된 확장자가 없으면 throw
					throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.File.FAIL_DENYFILEEXTENSION, "[\"" + fileExtension + "\"] 확장자는 지원하지 않습니다.");
				
				// 2. 카테고리+idx 폴더 생성 [여기서는 category를 안 쓴 이유는 Review안에 Foregin Key로 board category,idx를 각각 지정했기 때문에 리뷰 고유값인 idx만 가지고 폴더 생성]
				File saveFolder = new File(basicFolderLocation + "\\" + parentFolderName_Board_Review + "\\"+ reviewIdx); 
				saveFolderPath = saveFolder.getPath(); // Exception 발생 시 폴더 생성 rollback을 위해 값 주기. [Exception에서 Try문안에 선언된 변수 접근 불가.]
				if (saveFolder.exists() == false)
					saveFolder.mkdirs();

				try {
					// 3. 이미지 로컬에 저장
					multipartFile.transferTo(Paths.get(saveFolderPath + "\\" + fileName));
				} catch (Exception e) {
					throw new FileUploadException("['" + fileName + "'] 파일을 업로드 하지 못했습니다. 다시 시도 해주세요.");
				}

				// 4. 파일 해쉬 저장
				String hashFileString = null;
				try {
					hashFileString = fileUtils.ExtractFileHashSHA256(saveFolder.getPath() + "\\" + fileName);
				} catch (Exception ex) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - File");
				}

				// 5. 파일 이름 변경을 위한 path 생성
				Path originalFilePath = Paths.get(saveFolderPath + "\\" + fileName);
				Path hashFilePath = Paths.get(saveFolderPath + "\\" + hashFileString + "." + fileExtension);

				// 6. 예기치않은 문제로 이미 저장된 해쉬파일이 있는지 확인
				try {
					File file = new File(hashFilePath.toUri());
					if (file.exists()) 
					{
						File tempOldFile = new File(originalFilePath.toUri());
						tempOldFile.delete();
						throw new CustomRuntimeException(HttpStatus.CONFLICT, ResponseStatusCodeMsg.File.FAIL_DUPLICATEFILE, "['" + fileName + "'] 파일은 이미 업로드 되어있습니다.");
					}
				} catch (CustomRuntimeException ex) {
					throw ex;
				}catch (Exception ex) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - File");
				}

				// 7. 오리지날 이름 -> 해시파일 이름 변경
				try {
					Files.move(originalFilePath,hashFilePath);
				} catch (IOException e) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - File");
				}

				
				// 8. 파일 DB 저장
				try 
				{
					FileReviewDto fileReviewDto = new FileReviewDto();
					fileReviewDto.setBoard_review_idx(reviewIdx);
					fileReviewDto.setOrigin_file_name(fileName);
					fileReviewDto.setStored_file_name(hashFileString);
					fileReviewDto.setOrder(image_Order);
					
					Integer saveResult = fileMapper.Save_Review(fileReviewDto);
					if (saveResult == null) // [Fail == Save Failed]
						throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");

					image_Order++; // 이미지 카운터 증가
				} catch (CustomRuntimeException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
				}
			}
		} catch (CustomRuntimeException ex) {
			fileUtils.ForceFolderDelete(saveFolderPath);
			throw ex;
		}
		catch (Exception ex) {
			fileUtils.ForceFolderDelete(saveFolderPath);
			throw ex;
		}
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
//		Path hashPath = Paths.get(saveFolderLocation + "\\" + hashFileString + fileExtension);
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
//		Path hashPath = Paths.get(saveFolderLocation + "\\" + hashFileString + fileExtension);
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
	
	
	
	public byte[] Download(String fileName)
	{
		byte[] fileByte = null;
		String srcFileName = null;
		
		try {
			srcFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
			
			File file = new File("c:\\GALE\\File\\" +srcFileName);
			
			if(!file.exists())
				throw new FileUploadDuplicateException();
			
			fileByte = FileCopyUtils.copyToByteArray(file);
		} catch (FileUploadDuplicateException ex) {
			fileByte = null;

		} catch (Exception ex) {
			fileByte = null;
		}
		
		return fileByte;
		
	}
	
}
