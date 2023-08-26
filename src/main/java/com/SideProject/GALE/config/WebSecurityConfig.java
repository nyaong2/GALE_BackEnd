package com.SideProject.GALE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.SideProject.GALE.jwt.JwtFilter;
import com.SideProject.GALE.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity //기본 웹보안
@RequiredArgsConstructor 
public class WebSecurityConfig {
	
	private final JwtProvider jwtTokenProvider;
	
	String[] IGNORE_URL_ARRAY = {
			"/favicon.ico",
			"/auth/**",
			"/board/**",
			"/file/**",
	};
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		//https://jangjjolkit.tistory.com/m/26
		
		System.out.println("Spring Security Initialize");
		http
			.httpBasic().disable() // 기본 로그인 페이지 사용 x
			.csrf().disable() // token을 localstorage에 저장해서 사용하기에 csrf 사용안함.
			.formLogin().disable();
		http
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); 	// 세션 사용없이 토큰을 통해 데이터를 주고받기에 세션 StateLess

		http // Exception Handling 할때 예죄클래스 추가
			.exceptionHandling()
			.accessDeniedHandler(new CustomAccessDeniedHandler())
			.authenticationEntryPoint(new CustomAuthenticationEntryPoint());	//권한 없이 접근하는 것에 대한 예외 핸들링2
		
		http // http ServletRequest를 사용하는 요청에 대한 접근 제한 설정
			.authorizeRequests()
			.antMatchers(IGNORE_URL_ARRAY).permitAll() // 특정 url 허용
			.anyRequest().authenticated() //위를 제외한 나머지는 전부 인증이 필요하도록.
			.and()
			.addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // ID, Password 검사 전에 jwt 필터 먼저 수
		
		return http.build();
	}
	

}
