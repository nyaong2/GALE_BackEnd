package com.SideProject.GALE.model.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileReviewDto {
	private int board_review_number;
	private String origin_file_name;
	private String stored_file_name;
	private int order_number;
}
