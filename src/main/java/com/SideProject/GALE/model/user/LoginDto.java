package com.SideProject.GALE.model.user;


import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
	public String userid;
	public String password;
	
	public boolean IsNullData()
	{
		return (StringUtils.hasText(userid) && StringUtils.hasText(password)) ? false : true;
	}
    
}
