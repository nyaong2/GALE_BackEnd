package com.SideProject.GALE.exception.file;

public class FileUploadException extends RuntimeException {

    public FileUploadException() { super(); }
	
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
