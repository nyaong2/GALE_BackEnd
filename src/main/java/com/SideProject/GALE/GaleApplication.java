package com.SideProject.GALE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.SideProject.GALE.util.TimeUtils;

@SpringBootApplication
public class GaleApplication {
	public static boolean LOGMODE = true;
	
	public static void main(String[] args) {
		SpringApplication.run(GaleApplication.class, args);
		TimeUtils.SystemCurrentTimeSync();
		TimeUtils.RestartRedis();
		System.out.println("#Server Start \n");
	}
//	
//	@RestController
//	public class TestPage {
//			@GetMapping("/test")
//			public String Heartbeat() {
//				return "가동중";
//			}
//		}
	
}

