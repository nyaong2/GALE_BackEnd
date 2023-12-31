package com.SideProject.GALE.controller.file;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SideProject.GALE.components.io.utils.CustomInputStreamResource;
import com.SideProject.GALE.components.response.ResponseHeaders;
import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.model.file.FileDto;
import com.SideProject.GALE.service.file.FileService;

import lombok.RequiredArgsConstructor;

@RestController	//Controller = ResponseBody 미포함
@RequiredArgsConstructor
@RequestMapping(value = "/file")
public class FileController {
	
	//https:taetaetae.github.io/2019/07/21/spring-file-upload/
	private final FileService fileService;
	private final com.SideProject.GALE.components.response.ResponseService responseService;

	// upload는 Board에서 처리하기에 따로 처리하지 않음.
    
	
	
	@GetMapping(value = "/download/board")
	public ResponseEntity<?> downloadBoardImages(@RequestParam int board_Number, @RequestParam int order_Number) {
		//https://jaehoney.tistory.com/277
		// byte[]로 반환은 서버에서 파일을 읽은 데이터를 메모리에 올려야 하기 때문에 Resource 방식으로 이용.
		
		File imageFile = fileService.Download_BoardImage(board_Number, order_Number);
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(imageFile);
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
		
		return responseService.CreateImage(
				ResponseHeaders.DownloadImageHeader(imageFile)
				, ResCode.SUCCESS
				, inputStreamResource);
	}
	
	
	@GetMapping(value = "/download/board/review")
	public ResponseEntity<?> downloadBoardReviewImage(@RequestParam int board_Review_Number, @RequestParam int order_Number)
	{
		File imageFile = fileService.Download_BoardReviewImage(board_Review_Number, order_Number);
        
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(imageFile);
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
		
		return responseService.CreateImage(
				ResponseHeaders.DownloadImageHeader(imageFile)
				, ResCode.SUCCESS
				, inputStreamResource);
	}
	
}
