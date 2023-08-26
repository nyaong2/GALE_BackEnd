package com.SideProject.GALE.controller.file;


import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.file.DenyFileExtensionException;
import com.SideProject.GALE.exception.file.FileNotFoundException;
import com.SideProject.GALE.exception.file.FileUploadException;
import com.SideProject.GALE.exception.file.FileUploadDuplicateException;
import com.SideProject.GALE.service.ResponseService;
import com.SideProject.GALE.service.file.FileService;

import lombok.RequiredArgsConstructor;

@RestController	//Controller = ResponseBody 미포함
@RequiredArgsConstructor
@RequestMapping(value = "/file")
public class FileController {
	
	//https://taetaetae.github.io/2019/07/21/spring-file-upload/
	private final ResponseService responseService;
	private final FileService fileService;
	


	@PostMapping(value = "/upload")
	@Transactional
	public ResponseEntity upload(MultipartFile file) {
		boolean result = false;
		
		try {
			result = fileService.Save(file);
		} catch (DenyFileExtensionException ex) {
			return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.File.FAIL_DENYFILEEXTENSION, ex.getMessage());
		} catch (FileUploadException ex) {
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, ex.getMessage());	
		} catch (FileUploadDuplicateException ex) {
			return responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, ResponseStatusCodeMsg.File.FAIL_DUPLICATEFILE, ex.getMessage());	
		} catch (Exception e) {}
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "업로드에 성공하였습니다.");
	}
	
	@PostMapping(value = "/upload/profile")
	@Transactional
	public ResponseEntity uploadProfile(MultipartFile file) {
		boolean result = false;
		
		try {
			result = fileService.Save(file);
		} catch (DenyFileExtensionException ex) {
			return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.File.FAIL_DENYFILEEXTENSION, ex.getMessage());
		} catch (FileUploadException ex) {
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, ex.getMessage());	
		} catch (FileUploadDuplicateException ex) {
			return responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, ResponseStatusCodeMsg.File.FAIL_DUPLICATEFILE, ex.getMessage());	
		} catch (Exception e) {}
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "업로드에 성공하였습니다.");
	}

	
    @PostMapping("/multiupload")
	@Transactional
    public ResponseEntity uploadMultipleFiles(@RequestParam("file") MultipartFile[] files) {
    	boolean result = false;
    	
    	int count= 0;
    	for(MultipartFile file : files)
       {
    		try {
    			result = fileService.Save(file);
    		} catch (DenyFileExtensionException ex) {
    			return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.File.FAIL_DENYFILEEXTENSION, ex.getMessage());
    		} catch (FileUploadException ex) {
    			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, ex.getMessage());	
    		} catch (FileUploadDuplicateException ex) {
    			return responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, ResponseStatusCodeMsg.File.FAIL_DUPLICATEFILE, ex.getMessage());	
    		} catch (Exception e) {}
       }
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "test");
    }
	
    
	@GetMapping(value = "/download")
	public ResponseEntity download(@RequestParam String fileName) {
		byte[] fileByte = null;
		
		try {
			fileByte = fileService.Download(fileName);
		} catch (FileNotFoundException ex) {
			return responseService.CreateBaseEntity(HttpStatus.NOT_FOUND, null, ResponseStatusCodeMsg.File.FAIL_NOTFOUND, "파일을 찾을 수 없습니다.");
		}
		
		if(fileByte == null)
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "서버에서 처리하지 못했습니다. 다시 시도 해주세요.");	

		JSONArray jsary = new JSONArray(fileByte);

		return new ResponseEntity<>(fileByte,null,HttpStatus.OK);
		//return responseService.CreateListEntity(HttpStatus.CREATED, null, BoardResCode.SUCCESS, "test", jsary);
	}	
	
}
