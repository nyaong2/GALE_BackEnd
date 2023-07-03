package com.SideProject.GALE.service.planner;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.mapper.planner.PlannerMapper;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.planner.PlannerDetailsDto;
import com.SideProject.GALE.model.planner.PlannerDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlannerService {
	private final PlannerMapper plannerMapper;
	
	@Transactional
	public void Write(PlannerDto plannerDto, List<PlannerDetailsDto> listPlannerDetailsDto) throws Exception
	{
		try {
			// 1. 플래너 내용 등록
			int result = plannerMapper.Write(plannerDto);
			if(result < 0)
				throw new Exception();
			
			// 2. 플래너 내용 등록된 idx 가져오기
			Integer plannerIdx = plannerMapper.GetPlannerIdx(plannerDto);
			if(plannerIdx == null)
				throw new Exception();

			// 3. 각 아이템별로 1번에서 등록된 플래너의 idx로 모두 변경
			for(PlannerDetailsDto item : listPlannerDetailsDto)
				item.setPlanner_idx(plannerIdx);

			// 4. 날짜별 추가한 여행지 추가.
			result = plannerMapper.WriteDetails(listPlannerDetailsDto);
			if(result < 0)
				throw new Exception();		
			
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	@Transactional
	public void Delete(TokenDto tokenDto, int idx) throws CustomRuntimeException, Exception
	{
		try {
			PlannerDto plannerDto = plannerMapper.GetPlanner(idx);
			if(! tokenDto.getEmail().equals(plannerDto.getEmail()))
				throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, ResponseStatusCodeMsg.Auth.FAIL_UNAUTHORIZED, "로그인 되지 않았거나 잘못된 접근입니다.");
			
			int result = plannerMapper.Delete(idx);
			if(result == 0)
				throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, ResponseStatusCodeMsg.Planner.FAIL_BAD_REQUEST, "이미 삭제 처리되었거나 잘못된 접근입니다.");			
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
		
	}
	
	
}
