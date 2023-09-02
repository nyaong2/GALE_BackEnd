package com.SideProject.GALE.model.board;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReadDto extends BoardDto {
	private int satisfaction;
	private int service;
	private int price;
	private int congestion;
	private int accessibility;
	private int allAverage;
	private int reviewCount;
	private String imageArrayFileName;
	private List<BoardReviewConciseReadDto> reviewList;
}
