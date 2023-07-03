package com.SideProject.GALE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

	/*
	 * Spring5 부터 다양한 암호화 알고리즘을 변경할 수 있도록 생성방법이 변경됨. 이로인해 순환참조가 생겨서 따로 Bean을 뺐음.
	 * 기존 : WebSecurityConfig에 Bean으로 생성했음
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
