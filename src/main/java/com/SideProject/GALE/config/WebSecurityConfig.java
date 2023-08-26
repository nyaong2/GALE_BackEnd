package com.SideProject.GALE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.SideProject.GALE.jwt.JwtFilter;
import com.SideProject.GALE.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity //기본 웹보안
@RequiredArgsConstructor 
public class WebSecurityConfig {
	
	private final JwtProvider jwtTokenProvider;
	
	String[] IGNORE_URL_ARRAY = {
			"/",
			"/favicon.ico",
			"/user/**",
			"/board/**",
			"/file/**",
			"/planner/**",
	};
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		//https://jangjjolkit.tistory.com/m/26
		
		System.out.println("Spring Security Initialize");
		http
			.httpBasic().disable() // 기본 로그인 페이지 사용 x
			.csrf().disable() // [Cross Site Request forgery] : 인증된 사용자 토큰을 탈취해 위조된 요청을 보냈을 경우 파악해 방지하는 기능 off token을 localstorage에 저장해서 사용하기에 csrf 사용안함.
			.formLogin().disable(); // 로그인 폼 disabled
		http
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); 	// 세션 사용없이 토큰을 통해 데이터를 주고받기에 세션 StateLess

		http // Exception Handling 할때 커스텀 예외 추가. 
			.exceptionHandling() // 예외 핸들링은 아래의 2가지밖에 없음.
			.accessDeniedHandler(new CustomAccessDeniedHandler()) 			// 로그인은 했지만, 특정 api요청에 관해 로그인 한 유저의 권한이 부족할 때
			.authenticationEntryPoint(new CustomAuthenticationEntryPoint());	//로그인이 안 한 상태에서 특정 api를 요구할 때
		
		http // http ServletRequest를 사용하는 요청에 대한 접근 제한 설정
			.authorizeRequests()
			//.antMatchers("/planner/**").access("hasRole('ROLE_ADMIN')")
			.antMatchers(IGNORE_URL_ARRAY).permitAll() // 특정 url 허용
			.anyRequest().authenticated() //위를 제외한 나머지는 전부 인증이 필요하도록.
			.and()
			.addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // UsernamePasswordAuthenticationFilter에 가기전 커스텀 jwtFilter를 통과시켜라.
		
		return http.build();
	}
	

}
