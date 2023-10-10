package com.SideProject.GALE.model.board;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardReadListDto extends BoardDto {
	//Main에서 rising가져오는것 dto 전용
	private String firstImageUrl;
	private int view_count;
}
