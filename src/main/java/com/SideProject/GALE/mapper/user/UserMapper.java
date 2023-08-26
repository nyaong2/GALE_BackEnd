package com.SideProject.GALE.mapper.user;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.user.LoginDto;
import com.SideProject.GALE.model.user.UserDto;
import com.SideProject.GALE.model.user.UserProfileRequestDto;


@Mapper
public interface UserMapper {
	Optional<UserDto> findUserByUserid(String userid);
	Integer join(UserDto accountDTO);
	Integer findNickname(String nickname);
	String getUserAuthority(String email);
	
	int ProfileUpdate(UserProfileRequestDto userProfileRequestDto);
}
