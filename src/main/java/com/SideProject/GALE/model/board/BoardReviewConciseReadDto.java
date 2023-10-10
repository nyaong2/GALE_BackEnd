package com.SideProject.GALE.model.board;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReviewConciseReadDto extends BoardReviewDto {
	private String userNickname;
	private String userProfileImageUrl;
	private List<String> imageArrayUrl;
	
}
