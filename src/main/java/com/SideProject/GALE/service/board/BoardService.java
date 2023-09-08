package com.SideProject.GALE.service.board;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.jwt.JwtProvider;
import com.SideProject.GALE.mapper.board.BoardMapper;
import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.board.BoardReadDto;
import com.SideProject.GALE.model.board.BoardReadListDto;
import com.SideProject.GALE.model.board.BoardReviewConciseReadDto;
import com.SideProject.GALE.model.board.BoardReviewDetailDto;
import com.SideProject.GALE.model.board.BoardReviewDetailReadDto;
import com.SideProject.GALE.model.board.ReportReviewDto;
import com.SideProject.GALE.service.file.FileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	private final BoardMapper boardMapper;
	private final JwtProvider jwtProvider;
	private final FileService fileService;

	public List<BoardReadListDto> GetRisingCategoryList(int board_Category_Number, int currentPage) {
		List<BoardReadListDto> list = null;
		try {
			Map<String,Integer> map = new HashMap<String,Integer>();
			map.put("board_Category_Number", board_Category_Number);
			int calculationCursor= currentPage * 6; // 프론트 메인에는 6개씩 출력이 되므로 request의 currentPage의 값을 곱해 현재 cursor위치를 6칸씩 이동하도록 함.
			map.put("calculationCursor", calculationCursor);
			
			list = boardMapper.GetCategoryBoardDataList(map);
			if(list.size() < 1)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_DATA);
			
			for(BoardReadListDto readDto : list) // 파일명을 가지고 이미지 주소 url로 변환
			{
				String convertFirstImageUrl = 	fileService.CreateArrayBoardImageFileString(readDto.getBoard_number(), readDto.getFirstImageUrl());
				readDto.setFirstImageUrl( (StringUtils.hasText(convertFirstImageUrl)) ? convertFirstImageUrl : "null" );
			}
			
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}

		return list;
	}


	@Transactional(propagation = Propagation.NESTED)
	public void Write(HttpServletRequest request, BoardDto boardDto) {
		// 요청사항이 맞는지 확인
		if (boardDto.getUserid().equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);

		try {
			boardDto.setRegdate(LocalDateTime.now().withNano(0)); // 시간 설정. Second로 해야함. MilliSecond로 하면, 나노초때문에 idx
																	// select문 날릴 시 null값 뜸. db에서 그 nano초를 설정하거나
																	// Seconds로 설정해야 함.

			if(boardMapper.Write(boardDto) != 1)
				throw new Exception();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}

	}

	
	
	public BoardReadDto Read(int board_Number) {
		BoardReadDto queryBoardDto;
		
		try 
		{
			//보드 정보 가져오기
			queryBoardDto = boardMapper.Read(board_Number);
			
			if(queryBoardDto == null)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_DATA);

			//파일 url 작업
			String convertImageUrlAddress = fileService.CreateArrayBoardImageFileString(board_Number, queryBoardDto.getImageArrayFileName());
			queryBoardDto.setImageArrayFileName(convertImageUrlAddress);
						
			queryBoardDto.setAllAverage( //따로 Service에서 작업한 이유 : sql로 작업하면 한번더 값을 호출하기 때문에 속도 저하가 있을 것으로 보여서.
				(
						queryBoardDto.getSatisfaction()
						+ queryBoardDto.getService()
						+ queryBoardDto.getPrice()
						+ queryBoardDto.getCongestion()
						+ queryBoardDto.getAccessibility()
				) / 5 
			);
			
		}catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
		return queryBoardDto;
	}

	
	
	@Transactional(propagation = Propagation.NESTED)
	public void Update(HttpServletRequest request, BoardDto boardDto) {
		// 업데이트를 위한 게시물 인덱스 설정
		String queryUserid = boardMapper.GetBoardUserid(boardDto.getBoard_number());
		
		// 요청사항이 맞는지 확인 (작성자, 요청사항, 글 요청한 데이터 자기가 쓴 글인지 확인)
		if(StringUtils.hasText(queryUserid) == false)
			throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_DATA);			
		
		if ( queryUserid.equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false )
			throw new CustomRuntimeException(ResCode.FORBIDDEN_UNAUTHENTICATED_REQUEST);

		try {
			if(boardMapper.Update(boardDto) != 1)
				throw new Exception();
			
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(propagation = Propagation.NESTED)
	public void Delete(HttpServletRequest request, int board_Number) {	
		
		try 
		{
			String queryBoardUserId = boardMapper.GetBoardUserid(board_Number);
			
			if (queryBoardUserId == null)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_DATA);

			// 아이디 맞는지 확인
			if (queryBoardUserId.equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
				throw new CustomRuntimeException(ResCode.FORBIDDEN_UNAUTHENTICATED_REQUEST);

			
			if(boardMapper.Delete(board_Number) < 1)
				throw new Exception();
			
			fileService.Remove_BoardImagesFolder(board_Number);
			
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
	}

	// [Review]-------------------------------------------------------------------

	@Transactional(propagation = Propagation.NESTED)
	public void Write_Review(HttpServletRequest request, BoardReviewDetailDto boardReviewDetailDto) {

		// 아이디 맞는지 확인
		if (boardReviewDetailDto.getUserid().equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);

		try {
			boardReviewDetailDto.setRegdate(LocalDateTime.now().withNano(0)); // 시간 설정. Second로 해야함. MilliSecond로 하면, 나노초때문에
																		// idx select문 날릴 시 null값 뜸. db에서 그 nano초를 설정하거나
																		// Seconds로 설정해야 함.

			if( boardMapper.Write_Review(boardReviewDetailDto)  != 1)
				throw new Exception();
			
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	public List<BoardReviewConciseReadDto> Read_BoardReviewPagingList(int board_Number, String sortType, String orderType, int review_CurrentPage)
	{
		List<BoardReviewConciseReadDto> reviewDto;
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("board_Number", board_Number);
			map.put("sortType", sortType);
			map.put("orderType", orderType);
			int calculationCursor= review_CurrentPage * 5; // 프론트에는 1,2,3,4,5,6,7대로 순서대로가고 그것에 맞게 현재페이지 cursor 값을 설정해서 sql로 넘김. 5개씩 리뷰가 보이도록 했으니 *5로 했음.
			map.put("calculationCursor", calculationCursor);

			reviewDto = boardMapper.Read_BoardReviewPagingList(map);			
			if(reviewDto.size() < 1)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_REVIEW_DATA);
			
			for(BoardReviewConciseReadDto conciseDto : reviewDto)
			{
				String convertImageUrlAddress_Review = fileService.CreateArrayBoardReviewImageFileString(conciseDto.getBoard_review_number(), conciseDto.getImageArrayUrl());
				String convertProfileImageUrl = fileService.CreateUserProfileImageNameUrl(conciseDto.getUserProfileImageUrl());
				conciseDto.setImageArrayUrl( (StringUtils.hasText(convertImageUrlAddress_Review)) ? convertImageUrlAddress_Review : "null" );
				conciseDto.setUserProfileImageUrl( (StringUtils.hasText(convertProfileImageUrl)) ? convertProfileImageUrl : "null" );
			}
			
		} catch(CustomRuntimeException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);			
		}
		
		return reviewDto;
	}

	public BoardReviewDetailReadDto Read_Review(int board_Review_Number) {
		BoardReviewDetailReadDto queryBoardReviewDto;
		try {
			queryBoardReviewDto = boardMapper.Read_Review(board_Review_Number);
			
			if(queryBoardReviewDto == null)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_REVIEW_DATA);
			
			String convertImageUrlAddress_Review = fileService.CreateArrayBoardReviewImageFileString(queryBoardReviewDto.getBoard_review_number(), queryBoardReviewDto.getImageArrayUrl());
			String convertProfileImageUrl = fileService.CreateUserProfileImageNameUrl(queryBoardReviewDto.getUserProfileImageUrl());
			queryBoardReviewDto.setImageArrayUrl( (StringUtils.hasText(convertImageUrlAddress_Review)) ? convertImageUrlAddress_Review : "null" );
			queryBoardReviewDto.setUserProfileImageUrl( (StringUtils.hasText(convertProfileImageUrl)) ? convertProfileImageUrl : "null" );

			
		} catch (CustomRuntimeException ex) {
			throw ex;
		}  catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		return queryBoardReviewDto;
	}

	
	
	@Transactional(propagation = Propagation.NESTED)
	public void Delete_Review(HttpServletRequest request, int board_review_number) 
	{
		String queryBoardReviewUserId = boardMapper.GetBoardReviewUserid(board_review_number);
		if (StringUtils.hasText(queryBoardReviewUserId) == false)
			throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_REVIEW_DATA);

		// 아이디 맞는지 확인
		if (queryBoardReviewUserId.equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);

		try {
			if(boardMapper.Delete_Review(board_review_number) < 1)
				throw new Exception();
			
			fileService.Remove_BoardReviewImagesFolder(board_review_number);
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	public void Report_Review(HttpServletRequest request, ReportReviewDto reportReviewDto)
	{
		if (reportReviewDto.getReporter_userid().equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);
		try 
		{
			if ( boardMapper.Report_Review(reportReviewDto) != 1)
				 throw new Exception();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	// [부가기능] ---------------------------------------------------------------------------------
	public void AddWishPlace(HttpServletRequest request, int board_Number)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("userid", userid);
			map.put("board_Number", board_Number);

			if(boardMapper.AddUserWishPlace(map) != 1)
				throw new Exception();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	public void DelWishPlace(HttpServletRequest request, int board_Number)
	{
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("userid", userid);
			map.put("board_Number", board_Number);

			int result = boardMapper.DelUserWishPlace(map);
			
			if(result == 0)
				throw new CustomRuntimeException(ResCode.BAD_REQUEST_BOARD_ALREADYPROCESSED_WISHPLACE);
			else if (result != 1)
				throw new Exception();
			
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
}
