package com.SideProject.GALE.jwt;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.exception.CustomRuntimeException_Msg;
import com.SideProject.GALE.mapper.user.UserMapper;
import com.SideProject.GALE.model.user.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserMapper userMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws CustomRuntimeException
	{
        return userMapper.findUserByUserid(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new CustomRuntimeException_Msg(ResCode.BAD_REQUEST, "해당하는 유저를 찾을 수 없습니다."));
    }
	
	private UserDetails createUserDetails(UserDto userDto)
	{
		// Collection<? extends GrantedAuthority> authorities = Collections.singleton(
		// new SimpleGrantedAuthority("1"));
		return User.builder()
				.username(userDto.getUserid())
				.password(userDto.getPassword())
				.roles(userDto.getRole())
				.build();
	}
	
}
