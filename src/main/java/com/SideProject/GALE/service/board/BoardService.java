																																																																																																																																																																																																																																																																																																																																																																						package com.SideProject.GALE.service.board;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.auth.AuthMapper;
import com.SideProject.GALE.mapper.board.BoardMapper;
import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.board.BoardReviewDto;
import com.SideProject.GALE.util.TimeUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
	
	private final BoardMapper boardMapper;
	
	
	public List<BoardDto> GetUserPrivateList(int category, String email)
	{
		List<BoardDto> getAllList = null;
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("category", category);
		map.put("writer", email);
		
		try {
			getAllList = boardMapper.GetUserPrivateList(map);
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		}
		
		return getAllList;
	}
	
	
	public List<BoardDto> GetPublicList(int category)
	{
		List<BoardDto> getAllList = null;
		try {
			getAllList = boardMapper.GetPublicList(category);
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		}
		
		return getAllList;
	}
	
	
	public Integer GetIndex(BoardDto parameter_BoardDto)
	{
		Integer queryIdx = null;
		try {
			queryIdx = boardMapper.GetIndex(parameter_BoardDto);
		} catch(Exception ex) {
			queryIdx = null;
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		};		
		return queryIdx;
	}
	
	
	public Integer GetIndex(BoardReviewDto parameter_BoardReviewDto)
	{
		Integer queryIdx = null;
		try {
			queryIdx = boardMapper.GetIndex_Review(parameter_BoardReviewDto);
		} catch(Exception ex) {
			queryIdx = null;
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		};		
		return queryIdx;
	}
	
	
	@Transactional
	public boolean Write(BoardDto boardDto) 
	{
		int result = 0;
		try {
			boardDto.setRegdate(LocalDateTime.now().withNano(0)); // 시간 설정. Second로 해야함. MilliSecond로 하면, 나노초때문에 idx select문 날릴 시 null값 뜸. db에서 그 nano초를 설정하거나 Seconds로 설정해야 함.
			result = boardMapper.Write(boardDto);
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		}
		
		return (result == 1) ? true : false;	
	}
		
	
	public BoardDto Read(int idx)
	{
		BoardDto boardDto = new BoardDto();
		try {
			boardDto = boardMapper.Read(idx);
			//boardDto = boardDto.stream()
		} catch(Exception ex) {
			boardDto = null;
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		};		
		return boardDto;
	}
	
	
	public boolean Update(BoardDto boardDto)
	{
		int result = 0;
		try {
			result = boardMapper.Update(boardDto);
		} catch(Exception ex) {
			System.out.println("에러내용 : " + ex);
		};
		
		return (result == 1) ? true : false;
	}
	
	
	public boolean Delete(int idx)
	{
		int result = 0;
		try {
			result = boardMapper.Delete(idx);
		} catch(Exception ex) {};
		
		return (result == 1) ? true : false;
	}
	
	
	//Review-------------------------------------------------------------------
	
	@Transactional(propagation = Propagation.NESTED)
	public boolean Write(BoardReviewDto boardReviewDto) 
	{
		int result = 0;
		try {
			boardReviewDto.setRegdate(LocalDateTime.now().withNano(0)); // 시간 설정. Second로 해야함. MilliSecond로 하면, 나노초때문에 idx select문 날릴 시 null값 뜸. db에서 그 nano초를 설정하거나 Seconds로 설정해야 함.
			result = boardMapper.Write_Review(boardReviewDto);
		} catch(Exception ex) {
			throw new CustomRuntimeException(HttpStatus.SERVICE_UNAVAILABLE, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - Database");
		}
		
		return (result == 1) ? true : false;	
	}
	
	
	public boolean Delete_Review(int idx, String writer)
	{
		int result = 0;

		Map hashMap = new HashMap();
		hashMap.put("idx", idx);
		hashMap.put("writer", writer);
		
		try {
			result = boardMapper.Delete_Review(hashMap);
		} catch (Exception ex) {};
		
		return (result == 1) ? true : false;
	}
	
	
	public boolean Like_Review(int board_review_idx, String userid)
	{
		int result = 0;
		
		Map hashMap = new HashMap();
		hashMap.put("board_review_idx", board_review_idx);
		hashMap.put("userid", userid);
		
		try {
			result = boardMapper.Like_Review(hashMap);
		} catch (Exception ex) {};
		
		return (result == 1) ? true : false;
		
	}
}
