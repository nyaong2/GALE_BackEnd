package com.SideProject.GALE.controller.planner;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SideProject.GALE.components.response.ResponseService;
import com.SideProject.GALE.controller.user.UserController;
import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.model.planner.PlannerAllListDto;
import com.SideProject.GALE.model.planner.PlannerReadDetailsDto;
import com.SideProject.GALE.model.planner.PlannerWriteRequestDto;
import com.SideProject.GALE.service.file.FileService;
import com.SideProject.GALE.service.planner.PlannerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json") // 맨앞부분은 user로 매핑

public class PlannerController {
	private final PlannerService plannerService;
	private final ResponseService responseService;
	private final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	
	
	@PostMapping("/planner")
	public ResponseEntity<?> Write(HttpServletRequest request, @RequestBody PlannerWriteRequestDto requestDto)
	{

		plannerService.Write(request, requestDto.getPlanner(), requestDto.getListPlannerDetails());
		
		return responseService.Create(null, ResCode.SUCCESS, "여행 기획이 성공적으로 저장되었습니다.");
	}
	
	
	
	/* [모든 플래너 가져오기] */
	@GetMapping("/planner/list") //마이페이지의 전체적인 플래너 목록
	public ResponseEntity<?> AllList(HttpServletRequest request) 
	{
		List<PlannerAllListDto> data = plannerService.AllList(request);
		
        HashMap<String, List<PlannerAllListDto>> responseData = new HashMap<>();
        responseData.put("list", data);
        
        JSONObject resultObj = new JSONObject(responseData);
		return responseService.CreateList(null, ResCode.SUCCESS, null, resultObj);
	}
	
	
	/* [특정 플래너 클릭했을때 특정 플래너의 day별 리스트 정보] */
	@GetMapping("/planner")
	public ResponseEntity<?> Read(HttpServletRequest request, @RequestParam int planner_number) 
	{
		List<PlannerReadDetailsDto> getQueryData = plannerService.Read(request,planner_number);
		
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        for(PlannerReadDetailsDto obj : getQueryData)
	        multiValueMap.add("content", obj);
        
		JSONObject resultObj = new JSONObject(multiValueMap);

		return responseService.CreateList(null, ResCode.SUCCESS, null, resultObj);
	}
	
	
	@DeleteMapping("/planner/{planner_number}")
	public ResponseEntity<?> Delete(HttpServletRequest request, @PathVariable int planner_number)
	{
		plannerService.Delete(request, planner_number);
		
		return responseService.Create(null, ResCode.SUCCESS, "여행 기획이 성공적으로 삭제되었습니다.");
	}
	

}
