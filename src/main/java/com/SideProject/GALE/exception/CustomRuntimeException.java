package com.SideProject.GALE.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.SideProject.GALE.enums.ResCode;

import lombok.Getter;

@SuppressWarnings("serial")
@Getter
public class CustomRuntimeException extends RuntimeException{
	private final ResCode resCode;
	private final Logger logger = LoggerFactory.getLogger(CustomRuntimeException.class);
	
    public CustomRuntimeException(ResCode resCode) 
    {
        this.resCode = resCode;  
        logger.warn(resCode.toString());
    }

}
