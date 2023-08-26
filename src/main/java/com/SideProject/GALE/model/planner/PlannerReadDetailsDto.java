package com.SideProject.GALE.model.planner;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlannerReadDetailsDto extends PlannerDetailDto{
	private BigDecimal longitude;
	private BigDecimal latitue;
}
