package com.SideProject.GALE.model.board;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReviewReadDto
{
	private String userid;
	private LocalDateTime regdate;
	private String content;
	private int satisfaction;
	private int service;
	private int price;
	private int congestion;
	private int accessibility;
	private int imageCount;
}
