package com.SideProject.GALE.jwt;

import lombok.Getter;

public enum MDCRequestIp {
	REQUEST_IP("RequestIp");
	
	@Getter
	private String RequestIp;
	
	MDCRequestIp(String RequestIp) {
		this.RequestIp = RequestIp;
	}
}
