package com.SideProject.GALE.model.user;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자 생성
public class SignupDto extends UserDto {
	private  String confirmpassword;
	
	public boolean IsNullData()
	{
		return (StringUtils.hasText(userid) == false || 
					StringUtils.hasText(password) == false ||
					StringUtils.hasText(confirmpassword) == false || 
					StringUtils.hasText(nickname) == false
					)
					? false : true;
	}
	
	public boolean hasUnsatisfactoryLength()
	{
		//프론트에서 정한 길이를 충족하는지
		if (	(this.userid.length() > 0 &&userid.length() < 50)
				&& (this.nickname.length() > 0 && getNickname().length() < 10)
				&& (this.password.length() >= 8 && this.password.length() < 16)
				&& (this.confirmpassword.length() >= 8 && this.confirmpassword.length() < 16)
			)
			return true;
		
		return false;
	}
}
