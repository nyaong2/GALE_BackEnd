package com.SideProject.GALE.model.user;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileRequestDto extends UserDto
{
	private String newPassword;
	private String newPasswordConfirmation;
	private String encryptedPassword;
	
	public boolean isNullCheck()
	{
		if(StringUtils.hasText(this.getNickname()) == false 
			|| StringUtils.hasText(this.getPassword()) == false 
			|| StringUtils.hasText(newPassword) == false 
			|| StringUtils.hasText(newPasswordConfirmation) == false)
			return true; //4개중 1개라도 참이면 null.
		
		return false;
	}
}
