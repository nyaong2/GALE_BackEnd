package com.SideProject.GALE.controller.wishplace;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SideProject.GALE.components.response.ResponseService;
import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.model.planner.PlannerWriteRequestDto;
import com.SideProject.GALE.service.wishplace.WishplaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json") // 맨앞부분은 user로 매핑
public class WishplaceController {
	private final WishplaceService wishplaceService;
	private final ResponseService responseService;
	
	// [부가기능] ---------------------------------------------------------------------------------
	@PostMapping("/wishplace")
	public ResponseEntity<?> Add(HttpServletRequest request, 
			@RequestParam int board_Number)
	{
		wishplaceService.Add(request, board_Number);
		
		return responseService.Create(null, ResCode.SUCCESS, "위시플레이스가 정상적으로 등록됐습니다.");
	}
	
	@DeleteMapping("/wishplace/{board_Number}")
	public ResponseEntity<?> Del(HttpServletRequest request, 
			@PathVariable int board_Number)
	{
		wishplaceService.Del(request, board_Number);
		
		return responseService.Create(null, ResCode.SUCCESS, "위시플레이스가 정상적으로 제거됐습니다.");
	}
	
}
