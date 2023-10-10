package com.SideProject.GALE.mapper.planner;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.planner.PlannerAllListDto;
import com.SideProject.GALE.model.planner.PlannerDetailDto;
import com.SideProject.GALE.model.planner.PlannerDto;
import com.SideProject.GALE.model.planner.PlannerReadDetailsDto;

@Mapper
public interface PlannerMapper {
	

	int Write(PlannerDto plannerDto);
	int WriteDetails(List<PlannerDetailDto> plannerDetailDto); // Write되고나서 2차적으로 등록되야함.

	List<PlannerAllListDto> AllList(String email);
	List<PlannerReadDetailsDto> Read(int planner_number);
	String GetUserId(int planner_number);


	int Delete(int planner_number);
}
