package com.SideProject.GALE.model.board;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardRegionDto {
	private int board_number;
	private String locationname;
	private String locationaddress;
	public BigDecimal longitude;
	public BigDecimal latitude;
	private int allAverage;
	private String firstImageUrl;
}
