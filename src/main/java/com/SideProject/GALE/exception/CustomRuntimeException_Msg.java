package com.SideProject.GALE.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.SideProject.GALE.enums.ResCode;

import lombok.Getter;

@SuppressWarnings("serial")
@Getter
public class CustomRuntimeException_Msg extends RuntimeException{
	private final ResCode resCode;
	private final Logger logger = LoggerFactory.getLogger(CustomRuntimeException_Msg.class);
	
    public CustomRuntimeException_Msg(ResCode resCode, String message) 
    {
        super(message);
        this.resCode = resCode;
        logger.warn("[HttpStatus : {}] [Code : {}] [Message : {}]",resCode.getHttpStatus(), resCode.getHttpStatus().name(), message);
    }

}
