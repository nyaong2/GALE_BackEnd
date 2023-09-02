package com.SideProject.GALE.model.board;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReviewDto {
	private int board_review_number;
	private int board_number;
	private String userid;
	private LocalDateTime regdate;
	private String content;
	private int average;
}
