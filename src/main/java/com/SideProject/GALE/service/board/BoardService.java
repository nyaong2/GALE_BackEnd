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
import com.SideProject.GALE.model.board.BoardReviewDto;
import com.SideProject.GALE.model.board.BoardReviewReadDto;
import com.SideProject.GALE.model.board.ReportReviewDto;
import com.SideProject.GALE.service.file.FileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	private final BoardMapper boardMapper;
	private final JwtProvider jwtProvider;
	private final FileService fileService;

	public List<BoardDto> GetList(int board_Category) {
		List<BoardDto> list = null;
//		try {
//			list = boardMapper.GetPublicList(board_Category);
//		} catch (Exception ex) {
//			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
//		}

		return list;
	}

//	public Integer GetIndex(BoardDto parameter_BoardDto)
//	{
//		Integer queryIdx = null;
//		try {
//			queryIdx = boardMapper.GetIndex(parameter_BoardDto);
//		} catch(Exception ex) {
//			queryIdx = null;
//			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
//		};		
//		return queryIdx;
//	}

//	public Integer GetIndex(BoardReviewDto parameter_BoardReviewDto)
//	{
//		Integer queryIdx = null;
//		try {
//			queryIdx = boardMapper.GetIndex_Review(parameter_BoardReviewDto);
//		} catch(Exception ex) {
//			queryIdx = null;
//			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
//		};		
//		return queryIdx;
//	}

	@Transactional(propagation = Propagation.NESTED)
	public void Write(HttpServletRequest request, BoardDto boardDto) {
		// 요청사항이 맞는지 확인
		if (boardDto.getUserid().equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);

		try {
			boardDto.setRegdate(LocalDateTime.now().withNano(0)); // 시간 설정. Second로 해야함. MilliSecond로 하면, 나노초때문에 idx
																	// select문 날릴 시 null값 뜸. db에서 그 nano초를 설정하거나
																	// Seconds로 설정해야 함.
			int result = boardMapper.Write(boardDto);
			
			if(result != 1)
				throw new Exception();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}

	}

	public BoardReadDto Read(int board_Number) {
		BoardReadDto queryBoardDto;
		try {
			queryBoardDto = boardMapper.Read(board_Number);
			
			if(queryBoardDto == null)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_DATA);
				
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
			int result = boardMapper.Update(boardDto);
			if(result != 1)
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

			
			int result = boardMapper.Delete(board_Number);
			fileService.Remove_BoardImagesFolder(board_Number);
			
			if(result < 1)
				throw new Exception();
		} catch (CustomRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
		
	}

	// [Review]-------------------------------------------------------------------

	@Transactional(propagation = Propagation.NESTED)
	public void Write_Review(HttpServletRequest request, BoardReviewDto boardReviewDto) {

		// 아이디 맞는지 확인
		if (boardReviewDto.getUserid().equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);

		try {
			boardReviewDto.setRegdate(LocalDateTime.now().withNano(0)); // 시간 설정. Second로 해야함. MilliSecond로 하면, 나노초때문에
																		// idx select문 날릴 시 null값 뜸. db에서 그 nano초를 설정하거나
																		// Seconds로 설정해야 함.
			int result = boardMapper.Write_Review(boardReviewDto);
			
			if(result != 1)
				throw new Exception();
			
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	public List<BoardReviewDto> Read_BoardReviewPagingList(int board_Number, int review_CurrentPage)
	{
		List<BoardReviewDto> reviewDto;
		try {
			Map<String,Integer> map = new HashMap<String,Integer>();
			map.put("board_Number", board_Number);
			int calculationCursor= review_CurrentPage * 5; // 프론트에는 1,2,3,4,5,6,7대로 순서대로가고 그것에 맞게 현재페이지 cursor 값을 설정해서 sql로 넘김. 5개씩 리뷰가 보이도록 했으니 *5로 했음.
			map.put("calculationCursor", calculationCursor);

			reviewDto = boardMapper.Read_BoardReviewPagingList(map);
			
			if(reviewDto.size() < 1)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_FILE_BOARD);
		} catch(CustomRuntimeException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);			
		}
		
		return reviewDto;
	}

	public BoardReviewReadDto Read_Review(int board_Review_Number) {
		BoardReviewReadDto queryBoardReviewDto;
		try {
			queryBoardReviewDto = boardMapper.Read_Review(board_Review_Number);
			
			if(queryBoardReviewDto == null)
				throw new CustomRuntimeException(ResCode.NOT_FOUND_FILE_BOARDREVIEW);
			
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
		int result = 0;

		String queryBoardReviewUserId = boardMapper.GetBoardReviewUserid(board_review_number);
		if (StringUtils.hasText(queryBoardReviewUserId) == false)
			throw new CustomRuntimeException(ResCode.NOT_FOUND_BOARD_REVIEW_DATA);

		// 아이디 맞는지 확인
		if (queryBoardReviewUserId.equals(jwtProvider.RequestTokenDataParser(request).get("userid")) == false)
			throw new CustomRuntimeException(ResCode.BAD_REQUEST_NOTEQUALS_DATA);

		try {
			result = boardMapper.Delete_Review(board_review_number);
			
			if(result < 1)
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
			int result = boardMapper.Report_Review(reportReviewDto);
			if (result != 1)
				 throw new Exception();
		} catch (Exception ex) {
			throw new CustomRuntimeException(ResCode.INTERNAL_SERVER_ERROR);
		}
	}
}
