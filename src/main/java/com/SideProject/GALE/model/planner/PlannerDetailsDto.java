package com.SideProject.GALE.model.planner;

import java.sql.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlannerDetailsDto {
	private int planner_idx;
	private int select_category;
	private int select_idx;
	private Date startdate;
}
