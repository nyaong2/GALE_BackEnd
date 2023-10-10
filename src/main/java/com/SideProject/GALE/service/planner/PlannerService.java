package com.SideProject.GALE.service.planner;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.planner.PlannerMapper;
import com.SideProject.GALE.model.planner.PlannerAllListDto;
import com.SideProject.GALE.model.planner.PlannerDetailDto;
import com.SideProject.GALE.model.planner.PlannerDto;
import com.SideProject.GALE.model.planner.PlannerReadDetailsDto;
import com.SideProject.GALE.service.file.FileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlannerService {
	private final FileService fileService;
	private final PlannerMapper plannerMapper;
	private final JwtProvider jwtProvider;
	

	@Transactional
	public void Write(HttpServletRequest request, PlannerDto plannerDto, List<PlannerDetailDto> listPlannerDetailsDto)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		if(plannerDto.getUserid().equals(userid) == false)
			throw new CustomRuntimeException(ResCode.FORBIDDEN_UNAUTHENTICATED_REQUEST);
			
		try 
		{
			// 1. 플래너 내용 등록
			int writePlanner = plannerMapper.Write(plannerDto);
			
			if(writePlanner < 1)
				throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
			
			// 2. 각 여행지 추가 아이템별로 1번에서 등록된 플래너의 number로 모두 변경
			for(PlannerDetailDto item : listPlannerDetailsDto)
				item.setPlanner_number(plannerDto.getPlanner_number()); // useGeneratedKey 설정해둬서 Getter로 꺼내면, insert된 값으로 됨.

			// 3. 날짜별 추가한 여행지 추가.
			int writePlannerDetails = plannerMapper.WriteDetails(listPlannerDetailsDto);

			if(writePlannerDetails < 1)
				throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
			
		}  catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	public List<PlannerAllListDto> AllList(HttpServletRequest request)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();

		List<PlannerAllListDto> queryPlannerDto = null;
		try {
			queryPlannerDto = plannerMapper.AllList(userid);
			if(queryPlannerDto.size() < 1)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_PLANNER_DATA);
			
			for(PlannerAllListDto dto : queryPlannerDto) //이미지 주소 작업
			{
				dto.setImageArrayUrl(fileService.CreateArrayBoardImageFileString(dto.getFirst_Board_Number(), dto.getQueryOnly_ImageArrayUrl()));
				dto.setQueryOnly_ImageArrayUrl(null);// response 할 때 키값이 들어가지 않도록 설정. 위에서만 작업 후에 버리는 방향으로. mybatis에서 List<String>을 쓰기가 껄끄러워 이렇게 작업
			}
		} catch(CustomRuntimeException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		};
		
		return queryPlannerDto;		
	}
	
	public List<PlannerReadDetailsDto> Read(HttpServletRequest request, int planner_number)
	{
		 String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		 List<PlannerReadDetailsDto> queryPlannerDto = null;
		try 
		{
			String queryUserId = plannerMapper.GetUserId(planner_number);
			if(StringUtils.hasText(queryUserId) == false) //요청한 데이터에 유저아이디가 없는 경우 -> 게시물이 없는 경우에도 포함임.
				throw new CustomRuntimeException(ResCode.NOT_FOUND_PLANNER_DATA);

			if(queryUserId.equals(userid) == false) //토큰의 userid와 요청한 요청한 플래너의 유저아이디가 맞지 않을 경우
				throw new CustomRuntimeException(ResCode.FORBIDDEN_UNAUTHENTICATED_REQUEST);
			
			queryPlannerDto = plannerMapper.Read(planner_number);
			
			for(PlannerReadDetailsDto dto : queryPlannerDto)
			{
				dto.setImageArrayUrl(fileService.CreateArrayBoardImageFileString(dto.getBoard_number(), dto.getQueryOnly_ImageArrayUrl()));
				dto.setQueryOnly_ImageArrayUrl(null);// response 할 때 키값이 들어가지 않도록 설정. 위에서만 작업 후에 버리는 방향으로. mybatis에서 List<String>을 쓰기가 껄끄러워 이렇게 작업
			}
				
		} catch(CustomRuntimeException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		return queryPlannerDto;
	}
	
	
	@Transactional
	public void Delete(HttpServletRequest request, int planner_number)
	{
		 String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();

		try {
			String querUserId = plannerMapper.GetUserId(planner_number);
		
			if(StringUtils.hasText(querUserId) == false)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_PLANNER_DATA);
		
			if(querUserId.equals(userid) == false)
				throw new CustomRuntimeException(ResCode.FORBIDDEN_UNAUTHENTICATED_REQUEST);
			
			int deleteResult = plannerMapper.Delete(planner_number);
			if(deleteResult == 0)
				throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);			
			
		}catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
	}
	
}
