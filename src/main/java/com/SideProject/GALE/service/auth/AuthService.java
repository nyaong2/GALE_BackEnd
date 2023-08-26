package com.SideProject.GALE.service.auth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.auth.AuthMapper;
import com.SideProject.GALE.model.auth.LoginDto;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.auth.UserDto;
import com.SideProject.GALE.redis.RedisService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final AuthMapper accountMapper;
	private final JwtProvider jwtProvider;
	private final RedisService redisService;

		
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public String test(String email) {
		return accountMapper.getUserAuthority(email);
	}
	
	
	public Map<String,Object> Login(LoginDto loginDto) throws CustomRuntimeException {
		Map<String,Object> resultData = null;
		Map<String,Object> allData = null;
		
		//Request Data Null Check
		if(loginDto.NullChecking())
			throw new CustomRuntimeException(HttpStatus.BAD_REQUEST,ResponseStatusCodeMsg.Auth.FAIL_NULLDATA, "잘못된 요청 입니다.");

		// ID Check
		UserDto accountDTO = accountMapper.findUserByEmail(loginDto.getEmail())
							.orElseThrow(() -> new CustomRuntimeException(HttpStatus.UNAUTHORIZED,ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "이메일/비밀번호를 다시 확인해주세요."));
		
		//Redis Server TokenCheck
		if(!redisService.Get(loginDto.getEmail()).toLowerCase().equals("null"))
			throw new CustomRuntimeException(HttpStatus.CONFLICT,ResponseStatusCodeMsg.Auth.FAIL_DUPLICATION, "이미 로그인이 되어 있습니다.");
		
		// PW Check
		if(!passwordEncoder.matches(loginDto.getPassword(), accountDTO.getPassword()))
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED,ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "이메일/비밀번호를 다시 확인해주세요.");
		
		try {
			// Generate Token
			allData = jwtProvider.GenerateAllToken(loginDto.getEmail());
			resultData = new HashMap<String,Object>();
			resultData.put("email", allData.get("email"));
			resultData.put("accessToken", allData.get("accessToken"));
			resultData.put("refreshToken", allData.get("refreshToken"));
					
			
			// Redis Save RefreshToken
			redisService.Save(loginDto.getEmail(), String.valueOf(resultData.get("refreshToken")), jwtProvider.GetRtMilliSeconds());
		} catch (Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		}
		
		return resultData;
	}
	
	
	
	public boolean Logout(TokenDto tokenDto) throws CustomRuntimeException
	{
		boolean result = false;
		
		//Request Data Null Check
		if( !(StringUtils.hasText(tokenDto.getEmail())) )
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED,ResponseStatusCodeMsg.Auth.FAIL_NULLDATA, "잘못된 토큰입니다.");

		//Invalid Token Check
//		if(!tokenDto.getToken().toLowerCase().equals("admin")) 	//임시 개발자용. 액세스토큰 백업안해놨을때 admin 입력하면 제거되도록 넣음.
//		{
//			if(!jwtProvider.validateToken(tokenDto.getToken()))
//				throw new CustomRuntimeException(HttpStatus.BAD_REQUEST,ResponseStatusCodeMsg.Auth.FAIL_INVALIDTOKEN,"만료된 토큰입니다.");			
//		}
		
		//Redis RefreshToken Exist Check
		if(redisService.Get(tokenDto.getEmail()).toLowerCase().equals("null"))
		{
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "잘못된 접근이거나 이미 로그아웃 되었습니다.");			
		}
		
		//Redis RefreshToken Remove
		result = redisService.Del(tokenDto.getEmail());
			
		return result;
	}
	
	
	
	public boolean Signup(UserDto userDto) throws CustomRuntimeException
	{
		//Request Data Null Check
		if(userDto.NullChecking())
			throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.Auth.FAIL_NULLDATA, "잘못된 요청입니다.");			

		//Exist Email Check
		if(this.ExistEmail(userDto.getEmail()))
			throw new CustomRuntimeException(HttpStatus.CONFLICT, ResponseStatusCodeMsg.Auth.FAIL_DUPLICATION, "이미 가입된 유저입니다.");
	
		
		//Length Check
		else if( (!userDto.getPassword().equals(userDto.getConfirmpassword())) ||
				(userDto.getEmail().length() > 50) || (userDto.getPassword().length()) > 16 ||
				(userDto.getNickname().length() > 10))
			throw new CustomRuntimeException(HttpStatus.BAD_REQUEST,ResponseStatusCodeMsg.Auth.FAIL_OVERFLOWDATA, "잘못된 요청입니다.");
			
			
		// #Regular 1 - 영문, 특수문자, 숫자 포함 8자 이상
		Pattern regexPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,16}$");
		Matcher regexMatcher = regexPattern.matcher(userDto.getPassword());
		
		// #Regular 1 Check
		if(!regexMatcher.find())
			throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.Auth.FAIL_DENIALAUTHSATISFY, "비밀번호는 영문과 특수문자 숫자를 포함하여 8자 이상이어야 합니다.");

		// #Regular 2 - 특수문자 금지 확인
		Pattern regexPattern1 = Pattern.compile("\\W");
		Pattern regexPattern2 = Pattern.compile("[!@#$%^*+=-]");

		// #Regular 2 Check
		for(int i = 0; i < userDto.getPassword().length(); i++)
		{
			String charOneString = String.valueOf(userDto.getPassword().charAt(i));
			Matcher tempMatcher1 = regexPattern1.matcher(charOneString);
			
			if(tempMatcher1.find())
			{
				Matcher tempMatcher2 = regexPattern2.matcher(charOneString);
				if(!tempMatcher2.find())
					throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.Auth.FAIL_DENIALAUTHSATISFY, "비밀번호에 특수문자는 !@#$^*+=-만 가능합니다.");	
			}
		}
		
		try {
			//PW Encrypt
			userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
	
			//DB Signup
			if(accountMapper.joinUser(userDto) != null)
				return true;
			
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		}
		
		return false;
	}
	
	
	public boolean ExistEmail(String email) throws CustomRuntimeException
	{
		boolean result = false;
		
		try {
			result = accountMapper.findUserByEmail(email).isPresent() ? true : false;
		} catch (Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		return result;
	}
	
	
	public boolean ExistNickname(String nickName) throws CustomRuntimeException
	{
		boolean result = false;
		
		try {
			result = (accountMapper.findNicknameExist(nickName) > 0) ? true : false; // 1개라도 발견되면 true
		} catch (Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		return result;
	}
	

	public String ExpireCheckingRefreshTokenAndGenerateAccessToken(TokenDto tokenDto) throws CustomRuntimeException
	{
		//Request Data Null Check
		if(tokenDto.isSucessLogin() == false)
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "잘못된 요청입니다.");		
		
		String tokens = null;
		Jws<Claims> claims = null;
		String saveDbToken = null;
		
		try {
			saveDbToken = redisService.Get(tokenDto.getEmail());
			if(!StringUtils.hasText(saveDbToken))
				throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "잘못된 요청이거나 로그인 되어있지 않습니다.");
			claims = jwtProvider.decryptionToken(saveDbToken);
			if(claims == null) // 비밀번호 안맞으면 Exception 발생. Exception 발생 시 null 반환
				throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		} catch(CustomRuntimeException ex) {
			if(ex.getMessage().contains("만료"))
			{
				if(StringUtils.hasText(redisService.Get(tokenDto.getEmail())))
					redisService.Del(tokenDto.getEmail());
			}
		}
		
//		if (!getDbToken.getToken().equals(tokenDto.getToken()))
//			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_DIFFERENTTOKEN, "서버의 토큰과 요청한 토큰과 일치하지 않습니다.");

		if(!claims.getBody().get("email").equals(tokenDto.getEmail()))
			throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "잘못된 요청입니다.");

		
		if(jwtProvider.validateToken(tokenDto.getToken())) //만료가 안됐으면 재발급
		{
	    	Map<String, Object> payload = new HashMap<>();
	    	payload.put("email", tokenDto.getEmail());
	    	payload.put("role", 1);
	    	Date date = jwtProvider.CreateDate();
	    	tokens = jwtProvider.GenerateAccessToken(payload, date, date.getTime() + jwtProvider.GetAtMilliSeconds());
		}
			
		return tokens;
	}
	
	
	public boolean validateAccessToken(String tokens)
	{
		return this.jwtProvider.validateToken(tokens);
	}
	
}
