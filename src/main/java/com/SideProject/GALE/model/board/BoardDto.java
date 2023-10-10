package com.SideProject.GALE.model.board;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자 생성
public class BoardDto {
	public int board_number;
	public int board_category_number;
	public String userid;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "Asia/Seoul")
	public LocalDateTime regdate;
	public String locationname;
	public String locationaddress;
	public BigDecimal longitude;
	public BigDecimal latitude;
	
	private String queryOnly_ImageArrayUrl;
}
