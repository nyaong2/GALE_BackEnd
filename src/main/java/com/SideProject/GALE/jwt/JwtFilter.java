package com.SideProject.GALE.jwt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	
	//GenericFilterBean -> 필터 여러개 거침
	//OncePerRequestFilter -> 하나로만 될 수 있도록.
	
	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException 
	{
		response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:3000"); //요청 보내는 페이지 도메인 지정
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); //요청 보내는 페이지 도메인 지정
	    response.setHeader("Access-Control-Allow-Credentials", "true"); // Request의 Credential 방식이 사용되게 할 것인지 지정. (Request는 true로 요청이 왔는데 Response가 false면, Response는 클라이언트측에서 무시당함)

	    response.setHeader("Access-Control-Allow-Methods","GET, HEAD, POST, PUT, DELETE, OPTIONS"); // 메소드 설정
	    response.setHeader("Access-Control-Max-Age", "3600"); // 해당 시간동안은 Prelight 요청 보내지 않음. (브라우저에서 캐싱하고 있는 시간)
	    response.setHeader("Access-Control-Allow-Headers","content-type,*"); // 헤더 설정
		
	    
		SimpleDateFormat sdf = new SimpleDateFormat("[yyyy년MM월dd일 HH시mm분ss초] ");
		
		Date now = new Date();
		String nowTime = sdf.format(now);
		System.out.println(nowTime + "JwtFilter \r\n path : " + request.getServletPath());

	    if("OPTIONS".equalsIgnoreCase(request.getMethod()))
		{
			response.setStatus(HttpServletResponse.SC_OK);
			System.out.println(nowTime + "JwtFilter - OPTIONS");
			return;
		}
	    
	    //아래에서 CustomRuntimeException을 발생시키는 놈들 exception filter에서 걸리지 않게 해줘야됨.
	    Authentication authentication = null;

	    try { //jwtProvider에 customException들을 advice 시켜놨기 때문에 advice가는 곳으로 가지 않도록 try catch를 따로 지정하여 방지
	    	
			String token = jwtProvider.resolveToken(request);
        
			if (StringUtils.hasText(token)) 
			{
				 jwtProvider.validateToken(token);
				//유효한 토큰이면 토큰에서 email과 role을 꺼내와서 Authentication을 만든 후 SecurityContext에 Authentication 전달
				// https://gksdudrb922.tistory.com/217
				authentication = jwtProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception ex) {}
				
        
		if(request.getServletPath().startsWith("/auth") || request.getServletPath().startsWith("/board") 
				|| request.getServletPath().startsWith("/file") || request.getServletPath().startsWith("/planner"))
		{
			System.out.println("나 통과됐음 : " + request.getServletPath());
				//https://jacksonhong.tistory.com/54
			MDC.put(MDCKey.TRX_ID.getTrxId(), UUID.randomUUID().toString().substring(0,8));
			MDC.put(MDCRequestIp.REQUEST_IP.getRequestIp(), request.getRemoteAddr());
			MDC.put(MDCUserId.USER_ID.getUserId(), (authentication != null) ? authentication.getName() : null);
			chain.doFilter(request, response);
			MDC.remove(MDCKey.TRX_ID.getTrxId());
			MDC.remove(MDCRequestIp.REQUEST_IP.getRequestIp());
			MDC.remove(MDCUserId.USER_ID.getUserId());
			return;
		}
		
		chain.doFilter(request, response);
	}
}
	
