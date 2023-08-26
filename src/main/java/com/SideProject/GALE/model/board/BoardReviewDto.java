package com.SideProject.GALE.model.board;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReviewDto {
	private int idx;
	private int board_category;
	private int board_number;
	private String writer;
	private LocalDateTime regdate;
	private String content;
	private int grade;
	private int service;
	private int price;
	private int congestion;
	private int accessibility;
}
