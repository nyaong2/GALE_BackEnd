package com.SideProject.GALE.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.service.ResponseService;

import lombok.RequiredArgsConstructor;

//@ControllerAdvice
//@RequiredArgsConstructor
public class ExceptionHandlerController {
//	private final ResponseService responseService;
	
//    @ExceptionHandler(CustomRuntimeException.class)
//    public ResponseEntity CustomRuntimeExceptionHandler(CustomRuntimeException ex) {
//    	
//		return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null , ResponseStatusCodeMsg.Board.FAIL_BAD_REQUEST, ex.getMessage());
//    }
}
