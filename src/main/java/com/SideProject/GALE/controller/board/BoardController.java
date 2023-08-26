package com.SideProject.GALE.controller.board;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Propagation;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.SideProject.GALE.GaleApplication;
import com.SideProject.GALE.controller.HttpStatusCode.ResponseStatusCodeMsg;
import com.SideProject.GALE.exception.CustomRuntimeException;
import com.SideProject.GALE.exception.file.DenyFileExtensionException;
import com.SideProject.GALE.model.auth.TokenDto;
import com.SideProject.GALE.model.board.BoardDto;
import com.SideProject.GALE.model.board.BoardReviewDto;
import com.SideProject.GALE.service.ResponseService;
import com.SideProject.GALE.service.board.BoardService;
import com.SideProject.GALE.service.file.FileService;
import com.SideProject.GALE.util.DebugMsg;
import com.SideProject.GALE.util.TimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json")
@Slf4j
public class BoardController {
	
	private final BoardService boardService;
	private final FileService fileService;
	private final ResponseService responseService;
	
	//참조했던 사이트 : https://github.com/leejinseok/spring-vue/blob/master/src/main/java/com/example/vue/config/security/JwtAuthenticationFilter.java
	
	// redis https://bcp0109.tistory.com/328
	
	@GetMapping("/board")
	public ResponseEntity GetList(@AuthenticationPrincipal TokenDto tokenDto, @RequestParam int category) // 특정 카테고리 리스트 모두 불러오기
	{
		List<BoardDto> allList = new ArrayList<BoardDto>();
		
		//로그인 되어있을 시 private 게시판 가져옴
		if(tokenDto.isSucessLogin())
			allList = boardService.GetUserPrivateList(category,tokenDto.getEmail());

		// 비로그인 / 로그인 둘 다 통합적으로 public 게시물 가져옴
		for(BoardDto list : boardService.GetPublicList(category))
			allList.add(list);
		
		if(allList.size() == 0)
			return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.Board.FAIL_NOTFOUND, "불러올 데이터가 없습니다.");
		
		JSONArray arrayData = new JSONArray(allList);
		return responseService.CreateListEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "성공", arrayData);
	}
	
	
	
	@PostMapping("/board") // C : 쓰기
	@Transactional (rollbackFor = Exception.class)
	public ResponseEntity Write(@AuthenticationPrincipal TokenDto tokenDto, 
			@RequestPart(value = "data") BoardDto boardDto, 
			@RequestPart(value = "multipartFileList", required = false)  List<MultipartFile> listImageFile)
	{
		
		//1. 로그인이 됐을 때만 게시물을 쓸 수 있으므로 체킹후 토큰 정보가 없으면 fail || 
		if(tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		else if (boardDto.getWriter().equals(tokenDto.getEmail()) == false)// 로그인 한 사람과 게시물 Request 보낸사람과 동일한지 체크
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근입니다.");
		
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Write", "[ID : " + tokenDto.getEmail() + "]");
		
		boolean writeResult  = false;
		try 
		{
			//2. 글쓰기 db등록
			writeResult = boardService.Write(boardDto);
			
			//Result = Fail
			if(writeResult == false)
			{
				if(GaleApplication.LOGMODE)
					DebugMsg.Msg("BoardController - Write", (writeResult) ? "[Result : true]" : "[Result : false]");
				
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.Board.FAIL, "글쓰기 실패했습니다.");
			}
			
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Write", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Write", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		//3번 하기전 idx 가져오기. [DB 저장에 필요함]
		int writeIdx = boardDto.getIdx(); //getIdx가 가능한 이유 : insert문에 useGeneratedKeys="true" keyProperty="idx"를 선언해둬서 insert 후에 된 idx값이 들어옴.
		if(writeIdx < 1)
		{
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		//3. 이미지 전송여부 체크 후 있으면 저장로직 진행
		if(listImageFile != null && listImageFile.size() > 0)
		{
			boolean imageSaveResult = false;
			try {
				fileService.ImageSave(writeIdx, boardDto.getCategory(), listImageFile);
			} catch (CustomRuntimeException ex) {
				/*
				 * 수동 롤백한 이유 : Service에서 Throw 발생시 Controller의 Transactional이 발동하지 않고 바로 Return을 줘서 "Transaction rolled back because it has been marked as rollback-only"이 발생했지 않았나 싶음.
				 * 그러므로 Service에 Propagation.NESTED를 줘서 부모(Controller)에게서 Throw를 발생시켜 부모 + 자식 (Service) 모두다 Rollback되도록 함.
				 */				
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null , ResponseStatusCodeMsg.Board.FAIL_BAD_REQUEST, ex.getMessage());
			} catch (Exception ex) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null , ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
			}
		}

		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Write [Result = ", "true]");
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "글쓰기에 성공했습니다.");
	}
	
	// R : 읽어오기
	@GetMapping("/board/{idx}") 
	public ResponseEntity Read(@AuthenticationPrincipal TokenDto tokenDto, @PathVariable int idx)
	{

		BoardDto readData = null;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Read", "[index : " + idx + "]");
		
		
		//1. 게시물을 읽을 수 있는 권한이 있는지 확인
		// - 클래스 하나 만들어서 거기서 아이디를 통해 친구인지 일반 유저인지 등 판단 후 처리
		
		//2. 권한에 맞아떨어지면 게시물 쿼리에서 읽고 전송
		try {
			readData = boardService.Read(idx);
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Read", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Read", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		// Private이면서 요청한 사람의 이메일과 요청한 사람의 게시물이 서로 같지 않을 경우
//		if(readData.getAccesstype() == 0 && tokenDto.isSucessLogin() == false)
//			return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, ResponseStatusCodeMsg.Board.FAIL_FORBIDDEN, "글쓴이만 볼 수 있습니다.");
//		else if(readData.getAccesstype() == 0 && loginCheck && !tokenDto.getEmail().equals(readData.getWriter()))
//			return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, ResponseStatusCodeMsg.Board.FAIL_FORBIDDEN, "글쓴이만 볼 수 있습니다.");
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Read", (readData != null) ? "[Result : true]" : "[Result : false]");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
		JSONObject obj = new JSONObject(readData);
		return (readData != null) ? responseService.CreateListEntity(HttpStatus.OK, headers, ResponseStatusCodeMsg.SUCCESS, "읽기 성공 했습니다.", obj)
				: responseService.CreateBaseEntity(HttpStatus.NOT_FOUND, null, ResponseStatusCodeMsg.Board.FAIL_NOTFOUND, "잘못된 접근이거나 데이터를 찾을 수 없습니다.");
		/*
		 * https://sas-study.tistory.com/326
		 * 익명상태에서 private 접근 : 401
		 * 로그인은 되어있는데 권한이 없을 경우 : 403
		 */
		
	}
	
	
	
	// U : 업데이트
	@PatchMapping("/board/{idx}") 
	public ResponseEntity Update(@AuthenticationPrincipal TokenDto tokenDto, @PathVariable int idx, @RequestBody BoardDto boardDto) // 게시물 수정하기
	{
		

		//로그인이 되어있지 않으면 게시물은 수정 할 수 없음
		if(tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");

		boolean result = false;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Update", "[index : " + idx + "]");

		// 업데이트를 위한 게시물 인덱스 설정
		boardDto.setIdx(idx);
		
		try {
			BoardDto ReadBoardDto = boardService.Read(idx);
				
			// Private 글 접근시 요청한 사람이랑 요청한 게시물이랑 작성자가 맞는지 비교
			if (!tokenDto.getEmail().equals(ReadBoardDto.getWriter()))
				return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, ResponseStatusCodeMsg.Board.FAIL_FORBIDDEN,"잘못된 접근입니다.");			

			result = boardService.Update(boardDto);
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Update", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Update", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}		
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Update", (result) ? "[Result : true]" : "[Result : false]");
		
		return (result) ?
				responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "업데이트 성공")
				: responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.Board.FAIL_BAD_REQUEST, "업데이트 실패");
	}
	
	
	
	// D : 지우기
	@DeleteMapping("/board/{idx}") 
	public ResponseEntity Delete(@AuthenticationPrincipal TokenDto tokenDto, @PathVariable int idx) // 게시물 삭제하기
	{
		
		if(tokenDto == null || tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		
		boolean result = false;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Delete", "[index : " + idx + "]");
		
		try {
			BoardDto ReadBoardDto = boardService.Read(idx);
			
			//요청한 사람이랑 요청한 게시물이랑 작성자 맞는지 비교
			if(!tokenDto.getEmail().equals(ReadBoardDto.getWriter()))
				return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null, ResponseStatusCodeMsg.Board.FAIL_FORBIDDEN, "글쓴이만 글을 지울 수 있습니다.");
			
			result = boardService.Delete(idx);
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Delete", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Delete", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Delete", (result) ? "[Result : true]" : "[Result : false]");
		
		return (result) ?
				responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "게시물이 삭제되었습니다.")
				: responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null, ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error - DataBase");
	}
	
	
	// Review --------------------------------------------------------------------------------------------------------------------
	
	@PostMapping("/board/review")
	@Transactional
	public ResponseEntity Write(@AuthenticationPrincipal TokenDto tokenDto,
			@RequestPart("requestbody") BoardReviewDto boardReviewDto,
			@RequestPart(value = "multipartFileList", required = false)  List<MultipartFile> listImageFile) // required 기본값 true [true = 값이 무조건 있어야 함.] [false = null도 허용]
	{
		//https://velog.io/@shin6403/React-Form-Data-%EC%A0%84%EC%86%A1

		//1. 로그인이 됐을 때만 게시물을 쓸 수 있으므로 체킹후 토큰 정보가 없으면 fail
		if(tokenDto == null || tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		else if (boardReviewDto.getWriter().equals(tokenDto.getEmail()) == false) // 로그인 한 사람과 게시물 Request 보낸사람과 동일한지 체크
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근입니다.");

		//boolean boardWriteResult= false;
		
		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Write[Review]", "[ID : " + tokenDto.getEmail() + "]");
		
		
		boolean writeResult  = false;
		
		try 
		{
			//2. 글쓰기_Review db등록
			writeResult = boardService.Write(boardReviewDto);
			
			//Result = Fail
			if(writeResult == false)
			{
				if(GaleApplication.LOGMODE)
					DebugMsg.Msg("BoardController - Write[Review]", (writeResult) ? "[Result : true]" : "[Result : false]");
				
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.Board.FAIL, "글쓰기 실패했습니다.");
			}
			
		} catch (CustomRuntimeException ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Write[Review]", "CustomException", ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
			return responseService.CreateBaseEntity(ex.getHttpStatus(), null, ex.getCode(), ex.getMessage());
		} catch (Exception ex) {
			if(GaleApplication.LOGMODE)
				DebugMsg.Msg("BoardController - Write[Review]", "Exception", null, null, ex.getMessage(), null);
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		//3번 하기전 idx 가져오기. [DB 저장에 필요함]
		int writeIdx = boardReviewDto.getIdx(); //getIdx가 가능한 이유 : insert문에 useGeneratedKeys="true" keyProperty="idx"를 선언해둬서 insert 후에 된 idx값이 들어옴.
		if(writeIdx < 1)
		{
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null,  ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}

		System.out.println("인덱스 : " + writeIdx + " listImageFile null 체크 : " + (listImageFile == null ? true : false));
		
		//3. 이미지 전송여부 체크 후 있으면 저장로직 진행
		if(listImageFile != null && listImageFile.size() > 0)
		{
			boolean imageSaveResult = false;
			try {
				fileService.ImageSave(writeIdx, listImageFile);
			} catch (CustomRuntimeException ex) {
				/*
				 * 수동 롤백한 이유 : Service에서 Throw 발생시 Controller의 Transactional이 발동하지 않고 바로 Return을 줘서 "Transaction rolled back because it has been marked as rollback-only"이 발생했지 않았나 싶음.
				 * 그러므로 Service에 Propagation.NESTED를 줘서 부모(Controller)에게서 Throw를 발생시켜 부모 + 자식 (Service) 모두다 Rollback되도록 함.
				 */				
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return responseService.CreateBaseEntity(HttpStatus.FORBIDDEN, null , ResponseStatusCodeMsg.Board.FAIL_BAD_REQUEST, ex.getMessage());
			} catch (Exception ex) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null , ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
			}
		}

		if(GaleApplication.LOGMODE)
			DebugMsg.Msg("BoardController - Write [Result = ", "true]");
		
		return responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "글쓰기에 성공했습니다.");
	}
	
	
	@DeleteMapping("/board/review/{idx}")
	@Transactional
	public ResponseEntity Delete_Review(@AuthenticationPrincipal TokenDto tokenDto,
			@PathVariable int idx)
	{
		if(tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		
		boolean result = false;
		try {
			result = boardService.Delete_Review(idx, tokenDto.getEmail());
		} catch(Exception ex) {
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null , ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		return (result == true) ? responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "리뷰가 정상적으로 삭제되었습니다.")
			: responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.Board.FAIL_NOTFOUND, "이미 삭제되었거나 잘못된 요청입니다.");
	}
	
	
	@GetMapping("/board/review/like/{board_review_idx}")
	@Transactional
	public ResponseEntity Like_Review(@AuthenticationPrincipal TokenDto tokenDto,
			@PathVariable int board_review_idx)
	{
		if(tokenDto.isSucessLogin() == false)
			return responseService.CreateBaseEntity(HttpStatus.UNAUTHORIZED, null, ResponseStatusCodeMsg.Board.FAIL_UNAUTHORIZED, "잘못된 접근이거나 로그인 되지 않았습니다.");
		
		boolean result = false;
		try {
			result = boardService.Like_Review(board_review_idx, tokenDto.getEmail());
		} catch(Exception ex) {
			return responseService.CreateBaseEntity(HttpStatus.SERVICE_UNAVAILABLE, null , ResponseStatusCodeMsg.FAIL_SERVICE_UNAVAILABLE, "Server Error");
		}
		
		return (result == true) ? responseService.CreateBaseEntity(HttpStatus.OK, null, ResponseStatusCodeMsg.SUCCESS, "좋아요가 정상적으로 처리되었습니다.")
			: responseService.CreateBaseEntity(HttpStatus.BAD_REQUEST, null, ResponseStatusCodeMsg.Board.FAIL_NOTFOUND, "좋아요에 실패했습니다.");
	}
	
}
