package com.SideProject.GALE.controller.user;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.components.response.ResponseService;
import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.model.user.LoginDto;
import com.SideProject.GALE.model.user.SignupDto;
import com.SideProject.GALE.model.user.UserProfileRequestDto;
import com.SideProject.GALE.service.file.FileService;
import com.SideProject.GALE.service.user.UserService;

import lombok.RequiredArgsConstructor;


@RestController
//@RequiredArgsConstructor
@RequestMapping(value = "/user", produces = "application/json") // 맨앞부분은 user로 매핑
@RequiredArgsConstructor
public class UserController {
 
	//12-01 https://onejunu.tistory.com/138
	private final UserService userService;
	private final FileService fileService;
	private final ResponseService responseService;
	
	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	/* [로그인] */
	@PostMapping("/login")
	public ResponseEntity<?> Login(@RequestBody LoginDto loginDto) {

		Map<String,Object> token = userService.Login(loginDto);
		JSONObject tokenData = new JSONObject(token);
		return responseService.CreateList(null,ResCode.SUCCESS, null,tokenData);
	}
	
	
	/* [로그아웃] */
	@PostMapping("/logout")
	public ResponseEntity<?> Logout(HttpServletRequest request) 
	{	
		userService.Logout(request);
		
		return responseService.Create(null, ResCode.SUCCESS, null);
	}	
	

	/* [회원가입] */
	@PostMapping("/signup")
	@Transactional
	public ResponseEntity<?> Signup(@RequestBody SignupDto signupDto)
	{

		boolean result = userService.Signup(signupDto);

		return result ? responseService.Create(null, ResCode.SUCCESS, "회원가입이 완료됐습니다.")
							: responseService.Create(null, ResCode.BAD_REQUEST, "회원가입에 실패했습니다.");
	}
	
	
	
	/* [회원가입 - 이메일 중복체크] */
	@GetMapping("/signup/exist-userid")
	public ResponseEntity<?> ExistUserid(@RequestParam String userid)
	{
		boolean duplication = userService.ExistUserid(userid);

		return duplication ? responseService.Create(null, ResCode.DUPLICATION_USER_ID, null)
									: responseService.Create(null, ResCode.SUCCESS, "사용 가능한 이메일입니다.");
	}
	
	
	/* [회원가입 - 닉네임 중복체크] */
	@GetMapping("/signup/exist-nickname")
	public ResponseEntity<?> ExistNickname(@RequestParam String nickname)
	{
		boolean duplication =  userService.ExistNickname(nickname);
		
		return duplication ? responseService.Create(null, ResCode.DUPLICATION_USER_NICKNAME, null)
				: responseService.Create(null, ResCode.SUCCESS, "사용 가능한 닉네임입니다.");
	}
	
	
	/* [액세스토큰 재발급] */
	@RequestMapping(value= "/token")
	public ResponseEntity<?> RegenerationAccessToken(HttpServletRequest request)
	{
		// Request = 리프레시토큰 받고 검증 후 액세스토큰 다시 발급
		String token =  userService.RegenerationAccessToken(request);

		return responseService.CreateList(null, ResCode.SUCCESS, null, new JSONObject().put("accessToken",token));
	}
	
	
	// # - [프로필]
	
	@PatchMapping(value= "/profile")
	@Transactional
	public ResponseEntity<?> ProfileInformationUpdate(HttpServletRequest request, @RequestBody UserProfileRequestDto profileDto)
	{
		userService.ProfileUpdate(request, profileDto);

		return responseService.Create(null, ResCode.SUCCESS, "회원 정보가 정상적으로 수정되었습니다.");
	}
	
	
	@PostMapping(value= "/profile/image")
	@Transactional
	public ResponseEntity<?> ProfileImageUpdate(HttpServletRequest request, @RequestPart MultipartFile profileImageFile)
	{
		fileService.Update_ProfileImage(request, profileImageFile);

		 return responseService.Create(null, ResCode.SUCCESS, "프로필 사진이 정상적으로 등록됐습니다.");
	}
	
	@DeleteMapping(value= "/profile/image")
	@Transactional
	public ResponseEntity<?> ProfileImageUpdate(HttpServletRequest request)
	{
		fileService.Delete_ProfileImage(request);
		
		 return responseService.Create(null, ResCode.SUCCESS, "프로필 사진이 삭제됐습니다.");
	}

}


