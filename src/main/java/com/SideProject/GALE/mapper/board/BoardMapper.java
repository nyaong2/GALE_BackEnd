package com.SideProject.GALE.mapper.board;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.board.BoardReviewDto;

@Mapper
public interface BoardMapper {
	List<BoardDto> GetUserPrivateList(Map<String, Object> map);
	List<BoardDto> GetPublicList(int category);
	Integer GetIndex(BoardDto boardDto);
	Integer GetIndex_Review(BoardReviewDto boardDto);
	
	Integer Write(BoardDto boardDto);

	BoardDto Read(int idx);
	int Update(BoardDto boardDto);
	int Delete(int idx);
	
	
	//Review--------------------------------------------------------
	Integer Write_Review(BoardReviewDto boardDto);
	int Delete_Review(Map hashMap);
	
	int Like_Review(Map hashMap);
}
