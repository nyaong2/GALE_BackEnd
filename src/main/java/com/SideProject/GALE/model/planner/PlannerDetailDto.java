package com.SideProject.GALE.model.planner;

import java.sql.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlannerDetailDto {
	private int planner_number;
	private int board_number;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private Date startdate;
	private int order_number;
	
	private List<String> imageArrayUrl;
	private String queryOnly_ImageArrayUrl;
}
