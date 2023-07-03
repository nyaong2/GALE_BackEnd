package com.SideProject.GALE.jwt;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.SideProject.GALE.GaleApplication;
import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.util.TimeUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {
	
	@Value("${jwt.secret}")
	private String JWT_SECRETKEY;
	
	@Value("${jwt.accessToken_MilliSeconds}")
	private long JWT_ACCESS_MILLISECONDS;
	public long GetAtMilliSeconds() { return JWT_ACCESS_MILLISECONDS;}
	
	@Value("${jwt.refreshToken_MilliSeconds}")
	private long JWT_REFRESH_MILLISECONDS;
	public long GetRtMilliSeconds() { return JWT_REFRESH_MILLISECONDS;}
	
	@Value("${jwt.header}")
	private String JWT_TOKENHEADER;
	
	@Value("${jwt.bearer}")
	private String JWT_BEARER;
	
	private final UserDetailsService userDetailsService;
	
	private Map<String, Object> header = null;
	
    @PostConstruct
    protected void init() {
    	JWT_SECRETKEY = Base64.getEncoder().encodeToString(JWT_SECRETKEY.getBytes()); // SecretKey Base64로 인코딩
    	
    	//Header Setting
    	header = new HashMap<>();
    	header.put("typ", "JWT");
    	header.put("alg", "HS256");
    }
    
	//Claims : jwt에서 사용하는 구조를 쌍으로 만드는것.
	/* jwt 구조 : Header, Payload(Claims), Signature 
	 * Header : 토큰의 타입, 해시, 암호화 알고리즘으로 구성
	 * Payload : 토큰에 담을 클레임(Claim)정보 ex: 나이,이름 등)
	 *  - iss : 발급자
	 *  - sub : 제목
	 *  - aud : 대상자
	 *  - exp : 만료시간
	 *  - nbf : 토큰 활성날짜
	 *  - iat : 발급시간
	 *  - jti : 고유식별자 (중복처리 방지용)
	 * Signature : 비밀키를 포함하여 암호화 되어있음.
	 */
    
    public Date CreateDate()
    {
    	return new Date();
    }
    
    
    
    //Generate
    public Map<String,Object> GenerateAllToken(String email) {
    	Map<String, Object> tokens = new HashMap<String, Object>();
    	
    	Map<String, Object> payload = new HashMap<>();
    	payload.put("email", email);
    	payload.put("role", 1);
    	
    	Date date = this.CreateDate();
    	
    	long atExpiryMilliSeconds = (date.getTime() + JWT_ACCESS_MILLISECONDS);
    	long rtExpiryMilliSeconds = (date.getTime() + JWT_REFRESH_MILLISECONDS);
    	tokens.put("atExpiryMilliSeconds", atExpiryMilliSeconds);
    	tokens.put("rtExpiryMilliSeconds", rtExpiryMilliSeconds);
    	
    	
    	if(GaleApplication.LOGMODE)
    		System.out.println("[JwtTokenProvider - GenerateAllToken] 생성 시도 [ID : " + email + "]");

    	try{
    	// 호출된 Service에서 accessToken Response 및 RefreshToken을 DB에 저장하기 위헤 Map 형태로 put하여 데이터 리턴
    		tokens.put("email", email); //프론트엔드 요청으로 넣음.
    		tokens.put("accessToken", this.GenerateAccessToken(payload, date, atExpiryMilliSeconds));
    		tokens.put("refreshToken", this.GenerateRefreshToken(payload, date, rtExpiryMilliSeconds));
    		
    	}catch (Exception ex) {
    		System.out.println(ex);
    	}
    	return tokens;
    }
    
    
    
    public String GenerateAccessToken(Map<String,Object> payload, Date date, long expiryMilliSeconds)
    {
    	/*
    	 * date.getTime ,new Date(TimeUtils.GetCurrentMilliSeconds()).getTime() = MilliSeconds로 나옴
    	 * MilliSeconds = 1/1000 값. 여기서 /1000을 해야 초단위임. (MilliSeconds/1000 -> Seconds = 초단위)
    	 */
        if(GaleApplication.LOGMODE)
        	System.out.println("At 생성 시간 : " + TimeUtils.CurrentTimeStr(date.getTime()) + "만료 시간 : " + TimeUtils.CurrentTimeStr(expiryMilliSeconds) );
    	
        try{
    		return Jwts.builder()
    			.setHeader(header)
    			.setClaims(payload) // 정보 저장
    			.setIssuedAt(date) // 토큰 발행 시간정보
    			.setExpiration(new Date(expiryMilliSeconds)) // 토큰 유효시간 설정
    			.signWith(SignatureAlgorithm.HS256, JWT_SECRETKEY) // 알고리즘, 키값으로 서명할 것인지 설정
    			.compact(); // 압축
    	} catch(Exception ex) {
    		return null;
    	}
 
    }
    
    
    
    public String GenerateRefreshToken(Map<String,Object> payload, Date date, long expiryMilliSeconds)
    {
    	/*
    	 * date.getTime ,new Date(TimeUtils.GetCurrentMilliSeconds()).getTime() = MilliSeconds로 나옴
    	 * MilliSeconds = 1/1000 값. 여기서 /1000을 해야 초단위임. (MilliSeconds/1000 -> Seconds = 초단위)
    	 */
    	
        if(GaleApplication.LOGMODE)
        	System.out.println("Rt 생성 시간 : " + TimeUtils.CurrentTimeStr(date.getTime()) + "만료 시간 : " + TimeUtils.CurrentTimeStr(expiryMilliSeconds) );
    	
    	
    	try{
    		return Jwts.builder()
    			.setClaims(payload) // 정보 저장
    			.setIssuedAt(date) // 토큰 발행 시간정보
    			.setExpiration(new Date(expiryMilliSeconds)) // 토큰 유효시간 설정
    			.signWith(SignatureAlgorithm.HS256, JWT_SECRETKEY) // 암호화 방식
    			.compact(); // 압축
    	} catch(Exception ex) {
    		return null;
    	}
    }
    
    
    
    public String GetUserEmailToTokenConversion(String token) {
    	return Jwts.parser()
    			.setSigningKey(JWT_SECRETKEY)
    			.parseClaimsJws(token)
    			.getBody()
    			.getSubject();
    }
    
    
    
    public Jws<Claims> decryptionToken(String token) throws CustomRuntimeException {
    	try {
    		return Jwts.parser()
    				.setSigningKey(JWT_SECRETKEY)
    				.parseClaimsJws(token);
    		
    	} catch(ExpiredJwtException ex) { // 시간만료
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_INVALIDTOKEN, "요청하신 토큰이 만료되었습니다.");
    	} catch(SignatureException | MalformedJwtException | UnsupportedJwtException ex) { 
    		// Signature : 서버 비밀키로 안풀렸을 때 , Malformed = 구조 안맞는 토큰 , Unsupported = 지원하지않는 토큰
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_DIFFERENTTOKEN, "요청하신 토큰이 서버와 맞지 않는 토큰입니다.");
    	} catch(Exception e) {
    		return null;
    	}
    }
    
    
    
    public boolean validateToken(String token) throws CustomRuntimeException {
    	try {
    		Jws<Claims> claims = Jwts.parser().setSigningKey(JWT_SECRETKEY).parseClaimsJws(token);
    		return !claims.getBody().getExpiration().before(new Date()); //만료가 안됐을 경우 = true
    	} catch(ExpiredJwtException ex) { // 시간만료
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_INVALIDTOKEN, "요청하신 토큰이 만료되었습니다.");
    	} catch(SignatureException | MalformedJwtException | UnsupportedJwtException ex) { 
    		// Signature : 서버 비밀키로 안풀렸을 때 , Malformed = 구조 안맞는 토큰 , Unsupported = 지원하지않는 토큰
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_DIFFERENTTOKEN, "요청하신 토큰이 서버와 맞지 않는 토큰입니다.");
    	}
    }
    
    public Claims getClaims(String token) throws CustomRuntimeException {
    	try {
    		return Jwts.parser().setSigningKey(JWT_SECRETKEY).parseClaimsJws(token).getBody();
    	} catch(ExpiredJwtException ex) { // 시간만료
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_INVALIDTOKEN, "요청하신 토큰이 만료되었습니다.");
    	} catch(SignatureException | MalformedJwtException | UnsupportedJwtException ex) { 
    		// Signature : 서버 비밀키로 안풀렸을 때 , Malformed = 구조 안맞는 토큰 , Unsupported = 지원하지않는 토큰
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_DIFFERENTTOKEN, "요청하신 토큰이 서버와 맞지 않는 토큰입니다.");
    	}
    }
    
    //
    public Authentication GetAuthentication(String token) {
    	UserDetails userDetails = userDetailsService.loadUserByUsername(this.GetUserEmailToTokenConversion(token));    			
    	return new UsernamePasswordAuthenticationToken(token, "", userDetails.getAuthorities());
    }
    
}
