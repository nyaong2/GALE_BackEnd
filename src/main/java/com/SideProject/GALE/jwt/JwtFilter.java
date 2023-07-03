package com.SideProject.GALE.jwt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import com.SideProject.GALE.model.auth.AuthorityEnum;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.auth.UserDto;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {//GenericFilterBean {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
	private final JwtProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException 
	{
		System.out.println("아이피 : " + request.getLocalAddr() + " 두번째 : " + request.getRemoteAddr());
		Authentication authentication = getAuthentication(request);
		
		if(authentication != null) {
			SecurityContext context = SecurityContextHolder.getContext();
			context.setAuthentication(authentication);
		}
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("[yyyy년MM월dd일 HH시mm분ss초] ");
		
		Date now = new Date();
		String nowTime = sdf.format(now);
		System.out.println(nowTime + "JwtFilter \r\n path : " + request.getServletPath());
		//CorsSetting(request,response);
		
		//response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:3000"); //요청 보내는 페이지 도메인 지정
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); //요청 보내는 페이지 도메인 지정
        response.setHeader("Access-Control-Allow-Credentials", "true"); // Request의 Credential 방식이 사용되게 할 것인지 지정. (Request는 true로 요청이 왔는데 Response가 false면, Response는 클라이언트측에서 무시당함)
        response.setHeader("Access-Control-Allow-Methods","POST, GET"); // 메소드 설정
        response.setHeader("Access-Control-Max-Age", "3600"); // 해당 시간동안은 Prelight 요청 보내지 않음. (브라우저에서 캐싱하고 있는 시간)
        response.setHeader("Access-Control-Allow-Headers","content-type,*"); // 헤더 설정
        
        
        if("OPTIONS".equalsIgnoreCase(request.getMethod()))
        {
        	response.setStatus(HttpServletResponse.SC_OK);
        	System.out.println(nowTime + "JwtFilter - OPTIONS");
        }
        
		if(request.getServletPath().startsWith("/auth") || request.getServletPath().startsWith("/board") 
				|| request.getServletPath().startsWith("/file") || request.getServletPath().startsWith("/planner"))
		{
			System.out.println(request.getServletPath());
			filterChain.doFilter(request, response);
		}
		
	}
	
	private Authentication getAuthentication(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
		
        if (StringUtils.hasText(authorizationHeader) == false || authorizationHeader.startsWith(BEARER_PREFIX) == false) {
            return null;
        }
        
        String token = authorizationHeader.substring("Bearer ".length());
        Claims claims = null;
        try {
        	claims= jwtTokenProvider.getClaims(token);
        } catch (Exception ex) {
        	System.out.println("Filter - getAuthentication claims [Exception : " + ex.toString() + "]");
        };
		        
        Integer authorityValue = null;
        if(claims != null)
        	authorityValue= Integer.parseInt(claims.get("role").toString());
        	
        String role = null;
        Set<GrantedAuthority> roles = null;
        
        if (authorityValue != null)
        {
        	role = AuthorityEnum.GetIntegerAuthorityToString(authorityValue);
        	roles = new HashSet<>();
        	roles.add(new SimpleGrantedAuthority(role));
        }
        
        return new UsernamePasswordAuthenticationToken(new TokenDto(claims, token), null, roles);
		
	}
	
    // Request Header 에서 토큰 정보를 꺼내오기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
	
    public static String GetRemoteIp()
    {
		String ip = null;

		HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();		
		try {
			ip = req.getRemoteAddr();
		} catch (Exception ex){
			ip = null;
		}
		
		return ip;
    }
	
}
