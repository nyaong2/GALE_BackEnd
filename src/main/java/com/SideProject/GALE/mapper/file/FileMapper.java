package com.SideProject.GALE.mapper.file;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.file.FileDto;
import com.SideProject.GALE.model.file.FileReviewDto;

@Mapper
public interface FileMapper {
	Integer GetBoardImagesMaxOrder(FileDto fileDto);
	Integer GetBoardReviewImagesMaxOrder(Integer board_review_idx);
	BoardDto Read(int idx);
	Integer Save(FileDto fileDto);
	Integer Save_Review(FileReviewDto fileDto);
}
