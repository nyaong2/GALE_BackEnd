package com.SideProject.GALE.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.SideProject.GALE.GaleApplication;
import com.SideProject.GALE.util.DebugMsg;



@Service
public class ResponseService {
	public ResponseEntity CreateBaseEntity(HttpStatus httpStatus, @Nullable HttpHeaders httpHeaders, String code, String message)
	{
		Map<String, Object> responseData = new HashMap<String,Object>();
		responseData.put("code", code);
		responseData.put("message", message);
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("httpStatus : " + httpStatus + " code : " + code + " message : " + message);
		return (httpHeaders == null) ? ResponseEntity.status(httpStatus).body(responseData) : ResponseEntity.status(httpStatus).headers(httpHeaders).body(responseData);
	}

	public ResponseEntity CreateListEntity(HttpStatus httpStatus, @Nullable HttpHeaders httpHeaders, String code, String message, JSONObject data) {

		//Map<String, Object> responseData = new HashMap<String,Object>();
		JSONObject obj = new JSONObject();
		obj.put("code", code);
		obj.put("message", message);
		obj.put("data", data);
		
//		responseData.put("code", code);
//		responseData.put("message", message);
//		responseData.put("data", data);
		
		//HttpHeaders httpHeader = new HttpHeaders();
		//httpHeader.setContentType(MediaType.APPLICATION_JSON);
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("httpStatus : " + httpStatus + " code : " + code + " message : " + message);

		return (httpHeaders == null) ? ResponseEntity.status(httpStatus).body(obj.toString()) : ResponseEntity.status(httpStatus).headers(httpHeaders).body(obj.toString());
		//return ResponseEntity.status(httpStatus).headers(httpHeaders).body(obj.toString());
	}
		
	public ResponseEntity CreateListEntity(HttpStatus httpStatus, @Nullable HttpHeaders httpHeaders, String code, String message, JSONArray data) {

		//Map<String, Object> responseData = new HashMap<String,Object>();
		JSONObject obj = new JSONObject();
		obj.put("code", code);
		obj.put("message", message);
		obj.put("data", data);
		
		//		responseData.put("code", code);
		//		responseData.put("message", message);
		//		responseData.put("data", data);
		//HttpHeaders httpHeader = new HttpHeaders();
		//httpHeader.setContentType(MediaType.APPLICATION_JSON);
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("httpStatus : " + httpStatus + " code : " + code + " message : " + message);

		return (httpHeaders == null) ? ResponseEntity.status(httpStatus).body(obj.toString()) : ResponseEntity.status(httpStatus).headers(httpHeaders).body(obj.toString());
		//return ResponseEntity.status(httpStatus).headers(httpHeaders).body(obj.toString());
	}

}
