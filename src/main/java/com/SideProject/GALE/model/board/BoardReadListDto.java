package com.SideProject.GALE.model.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardReadListDto extends BoardDto {
	private String firstImageUrl;
	private int view_count;
}
