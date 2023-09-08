package com.SideProject.GALE.mapper.file;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.SideProject.GALE.model.file.FileDto;
import com.SideProject.GALE.model.file.FileUserProfileDto;
import com.SideProject.GALE.model.file.FileReviewDto;

@Mapper
public interface FileMapper {
	
	Integer Upload_Board(List<FileDto> fileDto);
	Integer Upload_Board_Review(List<FileReviewDto> fileReviewDto);
	
	FileDto Download_Board_ImageData(FileDto fileDto);
	FileReviewDto Download_Board_Review_ImageData(FileReviewDto fileReviewDto);
	
	Integer Save_UserProfileImage(FileUserProfileDto profileImageDto);
	FileUserProfileDto Read_UserProfile(String userid);
	Integer Delete_UserProfileImage(String userid);
}
