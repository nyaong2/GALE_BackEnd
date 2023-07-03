package com.SideProject.GALE.mapper.auth;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.auth.UserDto;


@Mapper
public interface AuthMapper {
	Optional<UserDto> findUserByEmail(String Email);
	Integer joinUser(UserDto accountDTO);
	Integer findNicknameExist(String nickname);
	String getUserAuthority(String email);
}
