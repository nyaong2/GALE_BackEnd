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
		//PasswordEncoderFactories.createDelegatingPasswordEncoder(); -> 스프링 시큐리티 기본 제공 팩토리 메서드
		return new BCryptPasswordEncoder(); // BCrypt 해시 알고리즘을 사용하여 비밀번호를 안전하게 인코딩하는 데에 사용
		//BCrypt = 1회 해시만 하는 것이 솔트를 부여해 여러번 해싱하여 좀 더 안전
	}
}
