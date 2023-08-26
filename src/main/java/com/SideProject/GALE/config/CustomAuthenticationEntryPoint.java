package com.SideProject.GALE.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.SideProject.GALE.components.response.ResponseService;
import com.SideProject.GALE.enums.ResCode;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	//로그인 안 한 상태에서 특정 api를 요구할 때 -> Authentication이 없는상태로 요구할 때
	

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		
        ResponseEntity<String> entity = new ResponseService().Create(null, ResCode.UNAUTHORIZED_SECURITY_UNAUTHORIZED_USER, null);

		entity.getHeaders().forEach((headerName, headerValue) -> {
            for (String value : headerValue) {
                response.addHeader(headerName, value);
            }
		});

		response.getWriter().write(entity.getBody().toString());
		response.setStatus(entity.getStatusCodeValue());
	}

}
