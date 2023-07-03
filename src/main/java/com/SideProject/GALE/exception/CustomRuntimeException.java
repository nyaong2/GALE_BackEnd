package com.SideProject.GALE.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomRuntimeException extends RuntimeException{
	private HttpStatus httpStatus;
	private String code;
	
    public CustomRuntimeException() {
        super();
    }
    
    public CustomRuntimeException(String message) {
        super(message);
    }

    public CustomRuntimeException(HttpStatus httpStatus, String code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public CustomRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomRuntimeException(Throwable cause) {
        super(cause);
    }
}
