package com.SideProject.GALE.model.planner;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlannerWriteRequestDto {
    private PlannerDto planner;
    private List<PlannerDetailDto> listPlannerDetails;
}
