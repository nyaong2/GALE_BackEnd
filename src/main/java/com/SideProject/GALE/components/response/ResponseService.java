package com.SideProject.GALE.components.response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.SideProject.GALE.enums.ResCode;


@Component
public class ResponseService {
	
	public ResponseEntity<String> Create(@Nullable HttpHeaders httpHeaders,  ResCode resCode, @Nullable String overrideMsg)
	{
		JSONObject resBody = new JSONObject();
		resBody.put("code",resCode.name());
		resBody.put("message", (overrideMsg != null ? overrideMsg : resCode.getMessage()));		//overrideMsg에 값을 넣을 경우 resCode에 정의된 메시지로 넣는 것이 아닌 overrideMsg로 들어가도록.
		return (httpHeaders == null) ? 
				ResponseEntity
					.status(resCode.getHttpStatus())
					.headers(ResponseHeaders.Json())
					.body(resBody.toString())
				:
				ResponseEntity
				.status(resCode.getHttpStatus())
				.headers(httpHeaders)
				.body(resBody.toString());
	}
	
	
	public ResponseEntity<String> CreateList(@Nullable HttpHeaders httpHeaders, ResCode resCode, @Nullable String overrideMsg, JSONArray data) {
		JSONObject resBody = new JSONObject();
		resBody.put("code",resCode.name());
		resBody.put("message", (overrideMsg != null) ? overrideMsg : resCode.getMessage());		//overrideMsg에 값을 넣을 경우 resCode에 정의된 메시지로 넣는 것이 아닌 overrideMsg로 들어가도록.
		resBody.put("data", data);
		
		return (httpHeaders == null) ? 
				ResponseEntity
					.status(resCode.getHttpStatus())
					.headers(ResponseHeaders.Json())
					.body(resBody.toString())
				:
				ResponseEntity
				.status(resCode.getHttpStatus())
				.headers(httpHeaders)
				.body(resBody.toString());
	}
	
	public ResponseEntity<String> CreateList(@Nullable HttpHeaders httpHeaders, ResCode resCode, @Nullable String overrideMsg, JSONObject data) {
		JSONObject resBody = new JSONObject();
		resBody.put("code",resCode.name());
		resBody.put("message", (overrideMsg != null) ? overrideMsg : resCode.getMessage());		//overrideMsg에 값을 넣을 경우 resCode에 정의된 메시지로 넣는 것이 아닌 overrideMsg로 들어가도록.
		resBody.put("data", data);
		
		return (httpHeaders == null) ? 
				ResponseEntity
					.status(resCode.getHttpStatus())
					.headers(ResponseHeaders.Json())
					.body(resBody.toString())
				:
				ResponseEntity
				.status(resCode.getHttpStatus())
				.headers(httpHeaders)
				.body(resBody.toString());
	}
	
	public ResponseEntity<?> CreateImage(HttpHeaders httpHeaders,  ResCode resCode, InputStreamResource imageFile)
	{
		return ResponseEntity
				.status(resCode.getHttpStatus())
				.headers(httpHeaders)
				.body(imageFile);
	}

}
