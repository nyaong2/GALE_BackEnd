package com.SideProject.GALE.mapper.planner;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.planner.GetAllListPlannerDto;
import com.SideProject.GALE.model.planner.PlannerDetailDto;
import com.SideProject.GALE.model.planner.PlannerDto;
import com.SideProject.GALE.model.planner.PlannerReadDetailsDto;

@Mapper
public interface PlannerMapper {
	
	List<GetAllListPlannerDto> GetAllPlannerList(String email);

	
	int Write(PlannerDto plannerDto);
	int Write_Details(List<PlannerDetailDto> plannerDetailDto);
	
	List<PlannerReadDetailsDto> Read(int planner_number);
	String GetUserId(int planner_number);


	int Delete(int planner_number);
}
