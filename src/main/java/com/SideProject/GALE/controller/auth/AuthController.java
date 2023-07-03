package com.SideProject.GALE.controller.auth;

import java.security.Principal;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SideProject.GALE.GaleApplication;
import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.model.auth.LoginDto;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.auth.UserDto;
import com.SideProject.GALE.service.ResponseService;
import com.SideProject.GALE.service.auth.AuthService;
import com.SideProject.GALE.util.DebugMsg;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins="http://localhost:3000")
@RequestMapping(value = "/auth", produces = "application/json") // 맨앞부분은 auth로 매핑
public class AuthController {
 
	//12-01 https://onejunu.tistory.com/138
	private final AuthService authService;
	private final ResponseService responseService;

	@PostMapping("/test")
	public ResponseEntity Test() {
		//System.out.println("값 : " + date);
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "성공");
	}
	
	

	@PostMapping("/login")
	public ResponseEntity Login(@RequestBody LoginDto loginDto) {
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Login", "[ID : " + loginDto.getEmail() + "]");
		
		Map<String,Object> token = null;
		
		try {
			token = authService.Login(loginDto);
		} catch (CustomRuntimeException ex){
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Login", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Login", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}

		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Login", (token == null) ? "[Result : false]" : "[Result : true]");

		
		
		JSONObject tokenData = new JSONObject(token);
		return (token != null) ? responseService.CreateListEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "로그인 성공", tokenData) 
							  : responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
	}
	
	
	//로그아웃
	@PostMapping("/logout")
	public ResponseEntity Logout(@AuthenticationPrincipal TokenDto tokenDto) 
	{	
		if(tokenDto == null)
			return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.Auth.FAIL_NULLDATA, "잘못된 접근입니다.");

		

		//로그인이 되어있지 않으면 로그아웃은 할 수 없음.
		if(tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "잘못된 접근이거나 이미 로그아웃 되었습니다.");

		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Logout", "[ID : " + tokenDto.getEmail() + "]");
			
		boolean result = false;
		
		try {
			result = authService.Logout(tokenDto);
		}catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Logout", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		}catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Logout", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		//HttpHeaders httpHeaders = new HttpHeaders();
		//httpHeaders.add("authorization", "gale " + tokens.get("accessToken"));
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Logout", "[Result : " + result + "]");

		
		return (result == true) ? responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "로그아웃 되었습니다.")
								: responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.Auth.FAIL, "로그아웃에 실패했습니다.");
	}	
	

	
	@PostMapping("/signup")
	public ResponseEntity Signup(@RequestBody UserDto userDto, Principal principal)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Signup", "[ID : " + userDto.getEmail() + ", PW : " + userDto.getPassword());		

		boolean result = false;

		try {
			result = authService.Signup(userDto);
		} 
		catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Signup", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Signup", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}

		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Signup", "[Result : " + result + "]");
		
		return result ? responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "회원가입 성공")
					: responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
	}
	
	
	
	//이메일 중복체크
	@GetMapping("/signup/exist-email")
	public ResponseEntity ExistEmail(@RequestParam String email)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - ExistEmail", "[ReqValue : " + email + "]");
		
		boolean duplication = false;
		try{
			duplication = authService.ExistEmail(email);
		}
		catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistEmail", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistEmail", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}

		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Signup", "[Duplication : " + duplication + "]");
		

		return duplication ? responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, ResponseStatusCodeMsg.Auth.FAIL_DUPLICATION, "이미 가입된 이메일입니다.")
					: responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "사용 가능한 이메일입니다.");
	}
	
	
	//닉네임 중복체크
	@GetMapping("/signup/exist-nickname")
	public ResponseEntity ExistNickname(@RequestParam String nickname)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - ExistNickname", "[ReqValue : " + nickname + "]");
			
		boolean duplication = false;
		try{
			duplication = authService.ExistNickname(nickname);
		}
		catch (CustomRuntimeException ex)
		{
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistNickname", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - ExistNickname", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}

		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - ExistNickname", "[Duplication : " + duplication + "]");

		return duplication ? responseService.CreateBaseEntity(HttpStatus.CONFLICT, null, ResponseStatusCodeMsg.Auth.FAIL_DUPLICATION, "이미 존재하는 닉네임입니다.")
					: responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "사용 가능한 닉네임입니다.");
	}
	
	
	//리프레시토큰을 이용한 액세스 토큰재발급
	@RequestMapping(value= "/token")
	public ResponseEntity ExpireCheckingRefreshTokenAndGenerateAccessToken(@AuthenticationPrincipal TokenDto tokenDto)
	{
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - Token", "[ReqValue : " + tokenDto.getEmail() + "]");

		// Request = 리프레시토큰 받고 검증 후 액세스토큰 다시 발급
		String tokens = null;
		
		try {
			tokens = authService.ExpireCheckingRefreshTokenAndGenerateAccessToken(tokenDto);
		}
		catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Token", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
			
		} catch(Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("AuthController - Token", "Exception", null, null, ex.getMessage(), null);

			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("AuthController - token", "[Result : " + (StringUtils.hasText(tokens) ? true : false) + "]");
		JSONObject obj = new JSONObject();
		
		return (StringUtils.hasText(tokens)) ? responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, tokens)
							: responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
	}
	
}


