package com.SideProject.GALE.model.auth;

import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenDto {
	
	private String email;
	private String token;
	private boolean sucessLogin;
	//public long IssuedTime = 0;
	//public long ExpirationTime = 0;	
    
	public TokenDto(Claims claims, String token) {
		if(claims != null)
		{
			this.setEmail( (claims.get("email") != null) ? claims.get("email").toString() : null );
			this.setToken( (!token.isEmpty()) ? token.toString() : null );
			this.setSucessLogin( ( StringUtils.hasText(email) && StringUtils.hasText(token) ) ? true : false);
		}
	}
    
}
