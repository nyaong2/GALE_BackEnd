package com.SideProject.GALE.model.planner;

import java.sql.Date;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlannerDto {
	private int idx;
	private String email;
	private String title;
	private Date date_start;
	private Date date_end;
	private LocalDateTime regdate;
}
