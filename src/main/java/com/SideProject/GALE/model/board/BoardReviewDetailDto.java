package com.SideProject.GALE.model.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReviewDetailDto extends BoardReviewDto
{
	private int satisfaction;
	private int service;
	private int price;
	private int congestion;
	private int accessibility;
}
