package com.SideProject.GALE.controller.planner;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.planner.PlannerDetailsDto;
import com.SideProject.GALE.model.planner.PlannerDto;
import com.SideProject.GALE.service.ResponseService;
import com.SideProject.GALE.service.planner.PlannerService;
import com.SideProject.GALE.util.kakaoMap.KakaoMapApi;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json")
public class PlannerController {
	private final ResponseService responseService;
	private final PlannerService plannerService;
	
	@PostMapping("/planner/test")
	public void test()
	{
		KakaoMapApi api = new KakaoMapApi();
		
		api.category();
	}
	
	@PostMapping("/planner/{idx}")
	public ResponseEntity Read(@AuthenticationPrincipal TokenDto tokenDto, @RequestBody PlannerDto plannerDto, @RequestBody List<PlannerDetailsDto> listPlannerDetailsDto)
	{
		try {
			plannerService.Write(plannerDto,listPlannerDetailsDto);
		} catch (Exception ex) {
			return  responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "서버에서 제대로 처리되지 않았습니다.");
		}
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "여행 기획이 성공적으로 저장되었습니다.");
	}
	
	
	@PostMapping("/planner")
	public ResponseEntity Write(@AuthenticationPrincipal TokenDto tokenDto, @RequestBody PlannerDto plannerDto, @RequestBody List<PlannerDetailsDto> listPlannerDetailsDto)
	{
		
		if(tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");

		//Email Setting
		plannerDto.setEmail(tokenDto.getEmail());
		
		try {
			plannerService.Write(plannerDto,listPlannerDetailsDto);
		} catch (Exception ex) {
			return  responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "서버에서 제대로 처리되지 않았습니다.");
		}
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "여행 기획이 성공적으로 저장되었습니다.");
	}
	
	@DeleteMapping("/planner/{idx}")
	public ResponseEntity Delete(@AuthenticationPrincipal TokenDto tokenDto, @PathVariable int idx)
	{
		if(tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		
		try {
			plannerService.Delete(tokenDto, idx);
		} catch (CustomRuntimeException ex) {
			return  responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());			
		} catch (Exception ex) {
			return  responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "서버에서 제대로 처리되지 않았습니다.");			
		}
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "여행 기획이 성공적으로 삭제되었습니다.");
	}
}
