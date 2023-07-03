package com.SideProject.GALE.mapper.planner;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.planner.PlannerDetailsDto;
import com.SideProject.GALE.model.planner.PlannerDto;

@Mapper
public interface PlannerMapper {
	Integer GetPlannerIdx(PlannerDto plannerDto);
	PlannerDto GetPlanner(int idx);
	int Write(PlannerDto plannerDto);
	int WriteDetails(List<PlannerDetailsDto> plannerDetailsDto);
	int Delete(int idx);
}
