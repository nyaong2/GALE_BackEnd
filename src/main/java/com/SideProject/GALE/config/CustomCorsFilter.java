package com.SideProject.GALE.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomCorsFilter {
	
//	@Bean
//	public CorsFilter corsFilter(){
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true); // Response할때 json을 자바스크립트에서 처리할 수 있게 할지 여부
//        config.addAllowedOrigin("http://localhost:3000"); //특정 ip 응답 허용
//        config.addAllowedHeader("*"); // 모든 header에 응답 허용
//        config.setMaxAge(36000L); // 한번 요청 승인 받고나서 지속시간
//        config.setAllowedMethods(Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")); // 해당 리스트들의 Method 허용
//        source.registerCorsConfiguration("/**", config);
//        
//        return new CorsFilter(source);
//	}
}
