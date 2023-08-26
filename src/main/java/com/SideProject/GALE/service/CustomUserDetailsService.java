package com.SideProject.GALE.service;

import java.util.Arrays;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.SideProject.GALE.exception.auth.LoginFailedException;
import com.SideProject.GALE.mapper.auth.AuthMapper;
import com.SideProject.GALE.model.auth.UserDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final AuthMapper accountMapper;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		System.out.println("CustomUserDetailsService - loadUserByUsername");
		return accountMapper.findUserByEmail(email)
				.map(user -> addAuthorities(user))
				.orElseThrow(() -> new LoginFailedException(email + "없음."));
	}
	
	private UserDto addAuthorities(UserDto userDto) {
		userDto.setAuthorities(Arrays.asList(new SimpleGrantedAuthority(Integer.toString(userDto.getRole()))));
		
		return userDto;
	}
	
}
