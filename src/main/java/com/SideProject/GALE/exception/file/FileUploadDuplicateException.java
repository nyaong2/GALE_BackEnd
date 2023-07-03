package com.SideProject.GALE.exception.file;

public class FileUploadDuplicateException extends RuntimeException {
    public FileUploadDuplicateException() { super(); }
	
    public FileUploadDuplicateException(String message) {
        super(message);
    }
    
    public FileUploadDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
