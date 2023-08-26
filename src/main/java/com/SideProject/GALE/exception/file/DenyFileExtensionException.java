package com.SideProject.GALE.exception.file;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;

public class DenyFileExtensionException extends RuntimeException {
  
	public DenyFileExtensionException() { super(); }
	
	public DenyFileExtensionException(String message) {
        super(message);
    }
	
	public DenyFileExtensionException(ResponseStatusCodeMsg.Board Board_StatusMsg, String message) {
        super(message);
    }
}
