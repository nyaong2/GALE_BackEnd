package com.SideProject.GALE.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomException;
import com.SideProject.GALE.exception.CustomRuntimeException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {
	
	@Value("${jwt.secret}")
	private String JWT_SECRETKEY;
	private SecretKey SECRETKEY_BYTE = null;
	
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
	
	//Token Header
    public  final String AUTHORIZATION_HEADER = "Authorization";
    public  final String BEARER_PREFIX = "Bearer ";
    
	private Map<String, Object> header = null;
	
    @PostConstruct
    protected void init() {
    	//JWT_SECRETKEY = Base64.getEncoder().encodeToString(JWT_SECRETKEY.getBytes()); // SecretKey Base64로 인코딩
    	//SECRETKEY_BYTE = Keys.hmacShaKeyFor(JWT_SECRETKEY.getBytes(StandardCharsets.UTF_8));
        //String decodedKey = Base64.getEncoder().encodeToString(JWT_SECRETKEY.getBytes()); // SecretKey Base64로 인코딩;
        byte[] decodedKeyBytes = JWT_SECRETKEY.getBytes(StandardCharsets.UTF_8);
        
        SECRETKEY_BYTE = new SecretKeySpec(decodedKeyBytes, "HmacSHA384");
    	//Header Setting
    	header = new HashMap<>();
    	header.put("typ", "JWT");
    	header.put("alg", "HS384");
    }
    
    // Request Header 에서 토큰 정보를 꺼내오기
    public String resolveToken(HttpServletRequest request) {
    	String resolveToken = null;
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
        	resolveToken =  bearerToken.substring(7);
        }
        if(StringUtils.hasText(resolveToken) == false)
        	throw new CustomRuntimeException(ResCode.BAD_REQUEST_NULLDATA);
        
        return resolveToken;
    }
    
    
	public Authentication getAuthentication(String token) {

        Claims claims = null;
        try {
        	claims= this.parseClaims(token);
        } catch (Exception ex) {
        	return null;
        };
//		        
//        Integer authorityValue = null;
//        if(claims != null)
//        	authorityValue= Integer.parseInt(claims.get("role").toString());
//        	
        String role = claims.get("role").toString();
        if(StringUtils.hasText(role) == false)
        	return null;
        Set<GrantedAuthority> roles = null;
        
//        	role = AuthorityEnum.GetIntegerAuthorityToString(authorityValue);
        	roles = new HashSet<>();
        	roles.add(new SimpleGrantedAuthority(role));
        
        
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(role.split("_"))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        UserDetails principal = new User(claims.get("userid").toString(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, null, roles);
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
    
    //Generate
    public Map<String,Object> GenerateAllToken(Authentication authentication) {
    	
    	Map<String, Object> tokens = new HashMap<String, Object>();
    	
    	Date date = new Date();  	
    	
    	Map<String, Object> payload = new HashMap<>(); // 만료시간은 GenerateToken에서 설정하는것으로.
    	payload.put("userid", authentication.getName()); //프론트 엔드 요청으로 넣음.
    	payload.put("role", authentication.getAuthorities().stream()
    								.map(GrantedAuthority::getAuthority)
    								.collect(Collectors.joining(",")));
    	payload.put("iat", date.getTime()); //발급시간
    	
    	// 호출된 Service에서 accessToken Response 및 RefreshToken을 DB에 저장하기 위헤 Map 형태로 put하여 데이터 리턴
    	try {
    		tokens.put("accessToken", this.GenerateAccessToken(payload, date));
    		tokens.put("refreshToken", this.GenerateRefreshToken(payload, date));
    	} catch (Exception ex) {
    		throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
    	}
    	return tokens;
    }
    
    
    
    public String GenerateAccessToken(Map<String,Object> payload,Date date) throws Exception
    {
		/*
		 * date.getTime ,new Date(TimeUtils.GetCurrentMilliSeconds()).getTime() =
		 * MilliSeconds로 나옴 MilliSeconds = 1/1000 값. 여기서 /1000을 해야 초단위임.
		 * (MilliSeconds/1000 -> Seconds = 초단위)
		 */
		return Jwts.builder()
    			.setHeader(header)
    			.setClaims(payload) // 정보 저장
    			.setExpiration(new Date (date.getTime() + JWT_ACCESS_MILLISECONDS)) // 만료시간
    			.signWith(SECRETKEY_BYTE,SignatureAlgorithm.HS384)
    			.compact();
    }
    
    
    
    public String GenerateRefreshToken(Map<String,Object> payload, Date date) throws CustomException
    {
    	/*
    	 * date.getTime ,new Date(TimeUtils.GetCurrentMilliSeconds()).getTime() = MilliSeconds로 나옴
    	 * MilliSeconds = 1/1000 값. 여기서 /1000을 해야 초단위임. (MilliSeconds/1000 -> Seconds = 초단위)
    	 */
		return Jwts.builder()
    			.setHeader(header)
    			.setClaims(payload) // 정보 저장
    			.setExpiration(new Date (date.getTime() + JWT_REFRESH_MILLISECONDS)) // 만료시간
    			.signWith(SECRETKEY_BYTE,SignatureAlgorithm.HS384)
    			.compact();
    }
    

    
    
//    public Jws<Claims> decryptionToken(String token) throws CustomRuntimeException {
//    	try {
//    		return Jwts.parser()
//    				.setSigningKey(JWT_SECRETKEY)
//    				.parseClaimsJws(token);
//    		
//    	} catch(ExpiredJwtException ex) { // 시간만료
//    		throw new CustomException("[JwtProvider - GenerateRefreshToken]", payload.get("userid").toString(), 
//    				HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.Auth.FAIL_SERVER_CREATETOKEN, "Server Error [Create Token]");    	} catch(SignatureException | MalformedJwtException | UnsupportedJwtException ex) { 
//    		// Signature : 서버 비밀키로 안풀렸을 때 , Malformed = 구조 안맞는 토큰 , Unsupported = 지원하지않는 토큰
//			throw new CustomRuntimeException("[JWT - decryptionToken]", HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_DIFFERENTTOKEN, "요청하신 토큰이 서버와 맞지 않는 토큰입니다.");
//    	} catch(Exception e) {
//    		return null;
//    	}
//    }
    
    
    
    public void validateToken(String token) {
    	try {
    		Jwts.parserBuilder().setSigningKey(SECRETKEY_BYTE).build().parseClaimsJws(token);
    	} catch(io.jsonwebtoken.security.SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
    		// [Security = 서명체크,만료된 토큰, 알수없는 키, 혀용되지않은 액세스]  [Malformed = 구조 안맞는 토큰]  [Unsupported = 지원하지않는 토큰] , [IllegalArgument = claims이 비어있는 경우]
			throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_UNSUPPORT_TOKEN);
    	} catch(ExpiredJwtException ex) { 
    		// 시간만료
			throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_INVALID_TOKEN);
    	}
    }
    
    
    public Claims parseClaims(String token){
    	Claims claims = null;
    	try {
    		claims =  Jwts.parserBuilder().setSigningKey(SECRETKEY_BYTE).build().parseClaimsJws(token).getBody();

    		if( Objects.isNull(claims.get("userid")) )
    			throw new UnsupportedJwtException(token);
    		
    	} catch(io.jsonwebtoken.security.SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
    		// [Security = 서명체크,만료된 토큰, 알수없는 키, 혀용되지않은 액세스]  [Malformed = 구조 안맞는 토큰]  [Unsupported = 지원하지않는 토큰] , [IllegalArgument = claims이 비어있는 경우]
			throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_UNSUPPORT_TOKEN);
    	} catch(ExpiredJwtException ex) { 
    		// 시간만료
			throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_INVALID_TOKEN);
    	}
    	
    	return claims;
    }
    
    public Claims RequestTokenDataParser(HttpServletRequest request)
    {
		String token = this.resolveToken(request);

		if(StringUtils.hasText(token) == false)
			throw new CustomRuntimeException(ResCode.NOT_FOUND_USER_NULLTOKEN);

		return this.parseClaims(token);
    }
    
}
