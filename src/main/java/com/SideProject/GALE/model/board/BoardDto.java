package com.SideProject.GALE.model.board;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자 생성
public class BoardDto {
	public int idx;
	public int category;
	public String writer;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	public LocalDateTime regdate;
	public String locationname;
	public String locationaddress;
	public BigDecimal longitude;
	public BigDecimal latitue;
	public String content;
	public int grade;
	public int service;
	public int price;
	public int congestion;
	public int accessibility;
	
}
