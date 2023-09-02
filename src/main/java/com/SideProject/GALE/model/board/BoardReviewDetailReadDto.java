package com.SideProject.GALE.model.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReviewDetailReadDto extends BoardReviewDetailDto {
	private String userNickname;
	private String userImageProfileUrl;
	private String imageArrayUrl;
}
