package com.SideProject.GALE.model.auth;

import org.springframework.util.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
	public String email;
	public String password;
	
	public boolean NullChecking()
	{
		return (!StringUtils.hasText(email) || !StringUtils.hasText(password)) ? true : false;
	}
}
