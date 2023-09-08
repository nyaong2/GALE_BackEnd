package com.SideProject.GALE.model.board;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardReviewConciseReadDto extends BoardReviewDto {
	private String userNickname;
	private String userProfileImageUrl;
	private String imageArrayUrl;
}
