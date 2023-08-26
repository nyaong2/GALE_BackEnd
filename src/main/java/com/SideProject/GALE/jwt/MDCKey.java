package com.SideProject.GALE.jwt;

import lombok.Getter;

public enum MDCKey {
	TRX_ID("TrxId");
	
	@Getter
	private String TrxId;
	
	MDCKey(String TrxId) {
		this.TrxId = TrxId;
	}
}
