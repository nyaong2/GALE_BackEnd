package com.SideProject.GALE.controller.board;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.enums.ResCode;
import com.SideProject.GALE.exception.CustomRuntimeException_Msg;
import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.board.BoardReadDto;
import com.SideProject.GALE.model.board.BoardReadListDto;
import com.SideProject.GALE.model.board.BoardRegionDto;
import com.SideProject.GALE.model.board.BoardReviewConciseReadDto;
import com.SideProject.GALE.model.board.BoardReviewDetailDto;
import com.SideProject.GALE.model.board.BoardReviewDetailReadDto;
import com.SideProject.GALE.model.board.ReportReviewDto;
import com.SideProject.GALE.service.board.BoardService;
import com.SideProject.GALE.service.file.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json")
//@Slf4j
public class BoardController {
	
	private final BoardService boardService;
	private final com.SideProject.GALE.components.response.ResponseService responseService;
	private final FileService fileService;
	
	//참조했던 사이트 : https://github.com/leejinseok/spring-vue/blob/master/src/main/java/com/example/vue/config/security/JwtAuthenticationFilter.java
	// redis https://bcp0109.tistory.com/328
	
	
	/* [특정 카테고리 목록 다 가져오기] */
	@GetMapping("/board/list")
	public ResponseEntity<?> GetRisingCategoryList(
			@RequestParam int board_Category_Number,
			@RequestParam int currentPage)
	{
		List<BoardReadListDto> queryRisingBoardList = boardService.GetRisingCategoryList(board_Category_Number, currentPage);

		HashMap<String, List<BoardReadListDto>> convertResponseListData = new HashMap<>();
        	convertResponseListData.put("list", queryRisingBoardList);
		
		return responseService.CreateList(null, ResCode.SUCCESS, null, new JSONObject(convertResponseListData));
	}
	
	@GetMapping("/board/region")
	public ResponseEntity<?> GetRegionList(@RequestParam int region_Number)
	{
		List<BoardRegionDto> queryRisingBoardList = boardService.GetRegionList(region_Number);
		
		return responseService.CreateList(null, ResCode.SUCCESS, null, new JSONArray(queryRisingBoardList));
	}
		
	
	
	/* [여행장소 글쓰기] */
	@PostMapping("/board") 
	@Transactional (rollbackFor = Exception.class)
	public ResponseEntity<?> Write( HttpServletRequest request,
											@RequestPart(value = "data") BoardDto boardDto,
										   @RequestPart(value = "imageFile", required = false)  List<MultipartFile> imageFile)
	{
		// 1. 글쓰기 db등록
		boardService.Write(request, boardDto);

		
		//2번 하기전 idx 가져오기. [DB 저장에 필요함]
		int board_Number = boardDto.getBoard_number(); //getIdx가 가능한 이유 : insert문에 useGeneratedKeys="true" keyProperty="idx"를 선언해둬서 insert 후에 된 idx값이 들어옴.
		if(board_Number < 1)
		{
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return responseService.Create(null, ResCode.INTERNAL_SERVER_ERROR, null);
		}
		
		//2. 이미지 전송여부 체크 후 있으면 저장로직 진행
		if(imageFile != null && imageFile.size() > 0) 
		{	
			for(MultipartFile file : imageFile)
			{
				if(file.getSize() <= 0)
					throw new CustomRuntimeException_Msg(ResCode.BAD_REQUEST, "파일 업로드가 제대로 되지 않은 요청입니다. 잠시 후에 다시 시도 해주세요.");	
			}
			
			fileService.Upload_BoardImages(board_Number, imageFile);
		}
		
		return responseService.Create(null,ResCode.SUCCESS, "글쓰기에 성공했습니다.");
	}
	
	
	/* [여행장소 글 읽어오기] */
	@GetMapping("/board") 
	public ResponseEntity<?> Read(@RequestParam int board_Number)
	{
		BoardReadDto readData = boardService.Read(board_Number);

		return (readData != null) ? responseService.CreateList(null, ResCode.SUCCESS, null, new JSONObject(readData))
											: responseService.Create(null, ResCode.NOT_FOUND_NULLDATA, null);	
	}
	
	
	
	/* [여행장소 글 수정] */
	@PatchMapping("/board") 
	public ResponseEntity<?> Update(HttpServletRequest request,
												@RequestBody BoardDto boardDto) // 게시물 수정하기
	{
		boardService.Update(request, boardDto);
		return responseService.Create(null, ResCode.SUCCESS, null);
	}
	
	
	
	// D : 지우기
	@DeleteMapping("/board/{board_number}") 
	public ResponseEntity<?> Delete(HttpServletRequest request,@PathVariable int board_Number) // 게시물 삭제하기
	{
			
		boardService.Delete(request, board_Number);
		
		return responseService.Create( null,ResCode.SUCCESS, "게시물이 삭제되었습니다.");
	}
	
	
	
	
	
	// [Review] --------------------------------------------------------------------------------------------------------------------
	
	
	
	
	/* [여행장소 리뷰쓰기] */
	@PostMapping("/board/review")
	@Transactional
	public ResponseEntity<?> Write_Review(HttpServletRequest request, 
			@RequestPart("data") BoardReviewDetailDto boardReviewDetailDto,
			@RequestPart(value = "imageFile", required = false)  List<MultipartFile> listImageFile) // required 기본값 true [true = 값이 무조건 있어야 함.] [false = null도 허용]
	{
		
		//1. 리뷰 쓰기
		boardService.Write_Review(request, boardReviewDetailDto);
			
		
		//2번 하기전 idx 가져오기. [파일 저장할 때 DB 저장에 board_Reivew_Number 값 필요함]
		int board_Review_Number = boardReviewDetailDto.getBoard_review_number(); //getIdx가 가능한 이유 : insert문에 useGeneratedKeys="true" keyProperty="idx"를 선언해둬서 insert 후에 된 idx값이 들어옴.
		if(board_Review_Number < 1)
		{
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return responseService.Create(null, ResCode.INTERNAL_SERVER_ERROR, null);
		}

		//3. 이미지 전송여부 체크 후 있으면 저장로직 진행
		if(listImageFile != null && listImageFile.size() > 0)
		{
			for(MultipartFile file : listImageFile)
			{
				if(file.getSize() <= 0)
					throw new CustomRuntimeException_Msg(ResCode.BAD_REQUEST, "파일 업로드가 제대로 되지 않은 요청입니다. 잠시 후에 다시 시도 해주세요.");	
			}
			
			fileService.Upload_ReivewImages(board_Review_Number, listImageFile);
		}

		return responseService.Create(null, ResCode.SUCCESS, "리뷰가 성공적으로 작성됐습니다.");
	}
	
	
	
	/* [여행게시물 리뷰 가져오기] */
	@GetMapping("/board/review/list")
	public ResponseEntity<?> Read_BoardReviewPagingList(HttpServletRequest request, 
			@RequestParam int board_Number,
			@RequestParam String sortType,
			@RequestParam String orderType,
			@RequestParam int currentPage
			)
	{
		List<BoardReviewConciseReadDto> reviewListDto = boardService.Read_BoardReviewPagingList(board_Number, sortType, orderType, currentPage);
		HashMap<String, Object> convertResponseListData = new HashMap<>();
    	convertResponseListData.put("list", reviewListDto);
    	convertResponseListData.put("reviewCount", reviewListDto.get(0).getResponseOnly_reviewCount());
    	
		return responseService.CreateList(null, ResCode.SUCCESS, null, new JSONObject(convertResponseListData));
	}

	
	/* 리뷰 구체적으로 가져오기*/
	@GetMapping("/board/review")
	public ResponseEntity<?> Read_Review(@RequestParam int board_Review_Number)
	{
		
		BoardReviewDetailReadDto reviewReadDto = boardService.Read_Review(board_Review_Number);
		return responseService.CreateList(null, ResCode.SUCCESS, null, new JSONObject(reviewReadDto));
				
	}
	
	
	
	@DeleteMapping("/board/review/{board_Review_Number}")
	@Transactional
	public ResponseEntity<?> Delete_Review(HttpServletRequest request,
			@PathVariable int board_Review_Number)
	{
		
		boardService.Delete_Review(request, board_Review_Number);
		return responseService.Create(null, ResCode.SUCCESS, "리뷰가 정상적으로 삭제되었습니다.");
		
	}
	
	

	@PostMapping("/board/review/report")
	public ResponseEntity<?> ReportReview(HttpServletRequest request, 
			@RequestBody ReportReviewDto reportReviewDto)
	{
		boardService.Report_Review(request, reportReviewDto);
		
		return responseService.Create(null, ResCode.SUCCESS, "신고가 정상적으로 접수되었습니다.");
	}
	
	
	

}
