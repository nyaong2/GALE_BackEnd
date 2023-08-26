package com.SideProject.GALE.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자 생성
public class UserDto {
	public String userid;
	public String password;
	public String nickname;
	public String role = "USER";
}
