package com.SideProject.GALE.config;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.SideProject.GALE.components.response.ResponseService;
import com.SideProject.GALE.enums.ResCode;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	// 로그인은 했지만, 특정 api요청에 관해 로그인 한 유저의 권한이 부족할 때

	@Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
		ResponseEntity<String> entity = new ResponseService().Create(null, ResCode.FORBIDDEN_SECURITY_UNAUTHENTICATED_USER, null);
        
		entity.getHeaders().forEach((headerName, headerValue) -> {
            for (String value : headerValue) {
                response.addHeader(headerName, value);
            }
		});
		
		response.getWriter().write(entity.getBody().toString());
		response.setStatus(entity.getStatusCodeValue());
    }
}
