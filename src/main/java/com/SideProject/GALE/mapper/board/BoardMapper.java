package com.SideProject.GALE.mapper.board;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.board.BoardReadDto;
import com.SideProject.GALE.model.board.BoardReadListDto;
import com.SideProject.GALE.model.board.BoardReviewConciseReadDto;
import com.SideProject.GALE.model.board.BoardReviewDetailReadDto;
import com.SideProject.GALE.model.board.BoardReviewDto;
import com.SideProject.GALE.model.board.ReportReviewDto;

@Mapper
public interface BoardMapper {
	
	List<BoardReadListDto> GetCategoryBoardDataList(Map<String,Integer> map);
	
	
	//Board
	Integer Write(BoardDto boardDto);
	
	String GetBoardUserid(int board_Number);
	BoardReadDto Read(int board_Number);
	
	int Update(BoardDto boardDto);
	
	int Delete(int board_Number);

	
	//Board_Review
	Integer Write_Review(BoardReviewDto boardReviewDto);
	
	List<BoardReviewConciseReadDto> Read_BoardReviewPagingList(Map<String,Object> map);
	List<BoardReviewConciseReadDto> Read_BoardReivewConciseList(int board_Number);
	String GetBoardReviewUserid(int board_Review_Number);
	BoardReviewDetailReadDto Read_Review(int board_Review_number);

	int Delete_Review(int board_Review_Number);
	
	int Report_Review(ReportReviewDto reportReviewDto);
	
	
	
	//Review--------------------------------------------------------
}
