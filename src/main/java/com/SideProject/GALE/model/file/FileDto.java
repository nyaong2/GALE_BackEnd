package com.SideProject.GALE.model.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileDto {
	private int board_idx;
	private int board_category;
	private String origin_file_name;
	private String stored_file_name;
	private int order;
}
