package com.SideProject.GALE.service.user;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.SideProject.GALE.components.io.utils.TimeUtils;
import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.user.UserMapper;
import com.SideProject.GALE.model.user.LoginDto;
import com.SideProject.GALE.model.user.SignupDto;
import com.SideProject.GALE.model.user.UserProfileInformationDto;
import com.SideProject.GALE.model.user.UserProfileRequestDto;
import com.SideProject.GALE.redis.RedisService;
import com.SideProject.GALE.service.file.FileService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class UserService {

	private final UserMapper userMapper;
	private final AuthenticationManagerBuilder authManagerBuilder;
	private final JwtProvider jwtProvider;
	private final RedisService redisService;
	private final FileService fileService;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	private void RegexUnfulfilledPasswordSecurity(String password)
	{
		// #Regular 1 - 영문, 특수문자, 숫자 포함 8자 이상
		Pattern regexPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,16}$");
		Matcher regexMatcher = regexPattern.matcher(password);
		
		// #Regular 1 Check
		if(regexMatcher.find() == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_USER_UNSATISFACTORY_PASSWORD);

		// #Regular 2 - 특수문자 금지 확인
		Pattern regexPattern1 = Pattern.compile("\\W");
		Pattern regexPattern2 = Pattern.compile("[!@#$%^*+=-]");

		// #Regular 2 Check
		for(int i = 0; i < password.length(); i++)
		{
			String charOneString = String.valueOf(password.charAt(i));
			Matcher tempMatcher1 = regexPattern1.matcher(charOneString);
			
			if(tempMatcher1.find())
			{
				Matcher tempMatcher2 = regexPattern2.matcher(charOneString);
				if(!tempMatcher2.find())
					throw new CustomRuntimeException(ResCode.BAD_REQUEST_USER_UNSATISFACTORY_SPECIALCHARACTER_PASSWORD);
			}
		}
	}
	
	
	
	
	// -------------------------------------------------------------------------------------------
	
	
	public Map<String,Object> Login(LoginDto loginDto) {
		Map<String,Object> tokenData = null;
		
		//Request Data Null Check
		if(loginDto.IsNullData())
			throw new CustomRuntimeException(ResCode.NOT_FOUND_NULLDATA);

		//Authentication 객체생성
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getUserid(), loginDto.getPassword());

		// 로그인 성공/실패 검증 authenticate 메서드 실행 될 때 CustomUserDetailsService에서 만든 loadUserByUsername 메서드 실행 (비밀번호 틀림은 loadUserByUsername에서 throw로 처리함)
		Authentication authentication = null;
		
		try {
			 authentication = authManagerBuilder.getObject().authenticate(authenticationToken);			
			/*메소드 실행시 AuthenticationManager 구현체인 ProviderManager 의 authenticate() 메소드가 실행됨.
			 * 해당 메소드는 AuthenticationProvider 인터페이스의 authenticate() 메소드 실행.
			 * 해당 인터페이스에서 데이터베이스의 이용자 정보를 가져오는 UserDetailsService를 사용.
			 * UserDetailsService 인터페이스의 loadUserByUsername() 메소드를 호출하게 됨.
			 */

		}  catch(BadCredentialsException ex) {
			throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_NOTMATCHED_LOGIN);
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		//Redis Server TokenCheck
		if(redisService.Get(loginDto.getUserid()).toLowerCase().equals("null") == false)
			redisService.Del(loginDto.getUserid());
		
		// Generate Token
		tokenData = jwtProvider.GenerateAllToken(authentication);

		// Redis Save RefreshToken
		redisService.Save(loginDto.getUserid(), String.valueOf(tokenData.get("refreshToken")), jwtProvider.GetRtMilliSeconds());
		
		return tokenData;
	}
	
	
	
	public void Logout(HttpServletRequest request)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();

		if(StringUtils.hasText(userid) == false)
			throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_UNSUPPORT_TOKEN);
		
		
		if(redisService.Get(userid).toLowerCase().equals("null") == false)
			redisService.Del(userid);
	}
	
	
	
	public boolean Signup(SignupDto signupDto)
	{
		boolean result = false;
		
		//Request Data Null Check
		if(signupDto.IsNullData() == false)
			throw new CustomRuntimeException(ResCode.NOT_FOUND_NULLDATA);

		//Exist Userid Check
		if( this.ExistUserid(signupDto.getUserid()) )
			throw new CustomRuntimeException(ResCode.DUPLICATION_USER_ID);
		
		//Length 체크
		if(signupDto.hasUnsatisfactoryLength() == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_USER_UNSATISFACTORY_LENGTH);
		
		if(signupDto.getPassword().equals(signupDto.getConfirmpassword()) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_USER_NOTEQUALS_PW_DATA);
		
		//비밀번호 보안 충족 체크
		this.RegexUnfulfilledPasswordSecurity(signupDto.getPassword());
		
		try {
			//PW Encrypt
			signupDto.setPassword(passwordEncoder.encode(signupDto.getPassword()));
			
			//DB Signup
			if(userMapper.join(signupDto) == 1)
				result = true;
			
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		return result;
	}
	
	
	
	public boolean ExistUserid(String userid)
	{
		if(StringUtils.hasText(userid) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NULLDATA);
		
		boolean result = false;
		
		try {
			result = userMapper.findUserByUserid(userid).isPresent();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		return result;
	}
	
	
	
	public boolean ExistNickname(String nickname)
	{
		if(StringUtils.hasText(nickname) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NULLDATA);
		
		boolean result = false;
		
		try {
			result = (userMapper.findNickname(nickname) > 0) ? true : false; // 1개라도 발견되면 true
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		return result;
	}
	
	
	
	public String RegenerationAccessToken(HttpServletRequest request)
	{
		String requestToken = jwtProvider.resolveToken(request);
		Claims parseToken = jwtProvider.parseClaims(requestToken); //parseClaims에 시간만료, 지원하지않는 토큰 등 검증함.

		String saveDbToken = null;
		String accessToken = null;
		
		try {
			saveDbToken = redisService.Get(parseToken.get("userid").toString());

			if(StringUtils.hasText(saveDbToken) == false)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_USER_LOGINTOKEN);
			if(saveDbToken.equals(requestToken) == false)
				throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_NOTMATCHED_TOKEN);
			
		} catch(CustomRuntimeException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);			
		}

		Map<String, Object> payload = new HashMap<>();
	    payload.put("userid", parseToken.get("userid"));
	    payload.put("role", parseToken.get("role"));
	    	
	    try {
	    	accessToken = jwtProvider.GenerateAccessToken(payload, TimeUtils.getDate());
	    	if(StringUtils.hasText(accessToken) == false)
	    		throw new Exception();
	    } catch (Exception ex) {
	    	throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
	    }
			
		return accessToken;
	}
	
	
	public UserProfileInformationDto ProfileInformation(HttpServletRequest request)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		UserProfileInformationDto userInformationDto;
		
		try {
			userInformationDto = userMapper.getProfileInformation(userid);
			
			if(userInformationDto == null)
				throw new Exception();
			
			userInformationDto.setProfileImageUrl(fileService.CreateUserProfileImageNameUrl(userInformationDto.getProfileImageUrl()));
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);			
		}
		
		return userInformationDto;
	}
	
	
	@Transactional(propagation = Propagation.NESTED)
	public void ProfileUpdate(HttpServletRequest request, UserProfileRequestDto profileDto)
	{
		if(profileDto.isNullCheck() == true)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NULLDATA);
			
		if(profileDto.getNewPassword().equals(profileDto.getNewPasswordConfirmation()) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_USER_NOTEQUALS_PW_DATA);
		
		//비밀번호 보안 충족 체크
		this.RegexUnfulfilledPasswordSecurity(profileDto.getNewPassword());
		
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		//Authentication 객체생성
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userid, profileDto.getPassword());
		
		try {
			 authManagerBuilder.getObject().authenticate(authenticationToken);
		}  catch(BadCredentialsException ex) {
			throw new CustomRuntimeException(ResCode.UNAUTHORIZED_USER_NOTMATCHED_MODIFYPROFILE);
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		try {
			profileDto.setUserid(userid);

			//비밀번호 암호화후 dto에 넣기.
			profileDto.setEncryptedPassword(passwordEncoder.encode(profileDto.getNewPassword()));
			if(userMapper.ProfileUpdate(profileDto) != 1)
				throw new Exception();
			
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);			
		}

	}
	
	
}
