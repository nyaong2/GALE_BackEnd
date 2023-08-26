package com.SideProject.GALE.model.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportReviewDto 
{
	private int report_number;
	private String reporter_userid;
	private int board_review_number;
	private int report_category;
	private String content;
}
