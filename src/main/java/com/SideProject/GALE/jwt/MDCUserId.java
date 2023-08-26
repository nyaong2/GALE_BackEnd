package com.SideProject.GALE.jwt;

import lombok.Getter;

public enum MDCUserId {
	USER_ID("UserId");
	
	@Getter
	private String UserId;
	
	MDCUserId(String UserId) {
		this.UserId = UserId;
	}
}
