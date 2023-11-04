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
import com.SideProject.GALE.model.board.BoardRegionDto;
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
				readDto.setFirstImageUrl(fileService.CreateSingleBoardImageFileString(readDto.getBoard_number(), readDto.getFirstImageUrl()));
			
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}

		return list;
	}
	
	public List<BoardRegionDto> GetRegionList(int region_Number)
	{
		List<BoardRegionDto> list = null;
		
		try {
			list = boardMapper.GetRegionBoardDataList(region_Number);
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);			
		}

		if(list.size() <= 0)
			throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_REGION_DATA);
		
		for(BoardRegionDto dto : list)
			dto.setFirstImageUrl(fileService.CreateSingleBoardImageFileString(dto.getBoard_number(), dto.getFirstImageUrl()));
		
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
			queryBoardDto.setImageArrayUrl(fileService.CreateArrayBoardImageFileString(board_Number, queryBoardDto.getQueryOnly_ImageArrayUrl()));
			queryBoardDto.setQueryOnly_ImageArrayUrl(null); // response 할 때 키값이 들어가지 않도록 설정. 위에서만 작업 후에 버리는 방향으로. mybatis에서 List<String>을 쓰기가 껄끄러워 이렇게 작업

			
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
			int boardReviewCount = boardMapper.GetBoardReviewCount(board_Number);
			
			if(boardReviewCount < 1)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_REVIEW_DATA);
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("board_Number", board_Number);
			map.put("sortType", sortType);
			map.put("orderType", orderType);
			int calculationCursor= review_CurrentPage * 5; // 프론트에는 1,2,3,4,5,6,7대로 순서대로가고 그것에 맞게 현재페이지 cursor 값을 설정해서 sql로 넘김. 5개씩 리뷰가 보이도록 했으니 *5로 했음.
			map.put("calculationCursor", calculationCursor);

			reviewDto = boardMapper.Read_BoardReviewPagingList(map);
			reviewDto.get(0).setResponseOnly_reviewCount(boardReviewCount);
			
			for(BoardReviewConciseReadDto conciseDto : reviewDto)
			{
				conciseDto.setImageArrayUrl(fileService.CreateArrayBoardReviewImageFileString(conciseDto.getBoard_review_number(), conciseDto.getQueryOnly_ImageArrayUrl()));
				conciseDto.setUserProfileImageUrl( fileService.CreateUserProfileImageNameUrl(conciseDto.getUserProfileImageUrl()));
				conciseDto.setQueryOnly_ImageArrayUrl(null); // response 할 때 키값이 들어가지 않도록 설정. 위에서만 작업 후에 버리는 방향으로. mybatis에서 List<String>을 쓰기가 껄끄러워 이렇게 작업
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
			
			String convertProfileImageUrl = fileService.CreateUserProfileImageNameUrl(queryBoardReviewDto.getUserProfileImageUrl());
			queryBoardReviewDto.setImageArrayUrl( fileService.CreateArrayBoardReviewImageFileString(queryBoardReviewDto.getBoard_review_number(), queryBoardReviewDto.getQueryOnly_ImageArrayUrl()));
			queryBoardReviewDto.setUserProfileImageUrl( (StringUtils.hasText(convertProfileImageUrl)) ? convertProfileImageUrl : "null" );
			queryBoardReviewDto.setQueryOnly_ImageArrayUrl(null); // response 할 때 키값이 들어가지 않도록 설정. 위에서만 작업 후에 버리는 방향으로. mybatis에서 List<String>을 쓰기가 껄끄러워 이렇게 작업
			
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
		String userid = jwtProvider.RequestTokenDataParser(request).get("userid").toString();
		
		if(StringUtils.hasText(userid) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);
		
		reportReviewDto.setReporter_userid(userid);
		try 
		{
			if ( boardMapper.Report_Review(reportReviewDto) != 1)
				 throw new Exception();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	

}
