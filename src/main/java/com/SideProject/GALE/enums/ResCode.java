package com.SideProject.GALE.enums;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

//https://bcp0109.tistory.com/303
@Getter
@AllArgsConstructor
public  enum ResCode {
	
	
	//200
	SUCCESS(HttpStatus.OK, ""),
	
	
	// 400
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	BAD_REQUEST_NOTEQUALS_DATA(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	BAD_REQUEST_NULLDATA(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	// [400 - User]
	BAD_REQUEST_USER_NOTEQUALS_PW_DATA(HttpStatus.BAD_REQUEST, "비밀번호가 서로 다릅니다."),
	BAD_REQUEST_USER_UNSATISFACTORY_LENGTH(HttpStatus.BAD_REQUEST, "서버에서 허용되지 않은 길이의 데이터 요청입니다."),
	BAD_REQUEST_USER_UNSATISFACTORY_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 영문과 특수문자 숫자를 포함하여 8자 이상 16자 이하이어야 합니다."),
	BAD_REQUEST_USER_UNSATISFACTORY_SPECIALCHARACTER_PASSWORD(HttpStatus.BAD_REQUEST,  "비밀번호에 특수문자는 !@#$^*+=-만 가능합니다."),
	// [400 - File]
	BAD_REQUEST_FILE_IMAGE_NULLDATA(HttpStatus.BAD_REQUEST, "정상적으로 요청이 되지 않은 이미지 파일이 포함되어있습니다."),
	BAD_REQUEST_FILE_NOTALLOW_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 확장자 입니다."),
	// [400 - Planner]
	BAD_REQUEST_PLANNER_NOTFOUND_BOARD(HttpStatus.BAD_REQUEST, "계획에 추가한 게시물이 존재하지 않습니다."),
	// [400 - Board]
	BAD_REQUEST_BOARD_ALREADYPROCESSED_WISHPLACE(HttpStatus.BAD_REQUEST, "이미 처리가 됐거나 존재하지 않은 위시플레이스의 요청입니다."),
	
	
	// 401
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ""),	
	UNAUTHORIZED_SECURITY_UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "권한이 없는 요청입니다."),
	
	// [401 - USER]
	UNAUTHORIZED_USER_NOTMATCHED_LOGIN(HttpStatus.UNAUTHORIZED,  "아이디 또는 비밀번호를 잘못 입력했습니다. 다시 확인 후 시도해주세요."),
	UNAUTHORIZED_USER_NOTMATCHED_MODIFYPROFILE(HttpStatus.UNAUTHORIZED,  "비밀번호를 잘못 입력했습니다. 다시 확인 후 시도해주세요."),
	
	UNAUTHORIZED_USER_UNSUPPORT_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 요청이거나 맞지 않는 토큰입니다."),
	UNAUTHORIZED_USER_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰 입니다."),
	UNAUTHORIZED_USER_NOTMATCHED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토근이거나 잘못된 요청입니다."),

	// 403
	FORBIDDEN(HttpStatus.FORBIDDEN, ""),
	FORBIDDEN_UNAUTHENTICATED_REQUEST(HttpStatus.UNAUTHORIZED, "권한이 없는 요청입니다."),

	FORBIDDEN_SECURITY_UNAUTHENTICATED_USER(HttpStatus.FORBIDDEN, "권한이 없는 요청입니다."),

	// [403 - USER]
	FORBIDDEN_USER_DENYAUTHENTICATION(HttpStatus.FORBIDDEN, "잘못된 접근이거나 잘못된 요청입니다."),
	
	// 404
	NOT_FOUND(HttpStatus.NOT_FOUND,""),
	NOT_FOUND_NULLDATA(HttpStatus.NOT_FOUND, "데이터가 없거나 잘못된 요청입니다."),

	// [404 - USER]
	NOT_FOUND_USER_NULLTOKEN(HttpStatus.NOT_FOUND, "로그인이 되지 않은 상태의 요청입니다."),
	NOT_FOUND_USER_LOGINTOKEN(HttpStatus.NOT_FOUND, "잘못된 요청입니다."),

	// [404 - BOARD]
	NOT_FOUND_BOARD_DATA(HttpStatus.NOT_FOUND, "게시물이 존재하지 않은 요청입니다."),
	NOT_FOUND_BOARD_REVIEW_DATA(HttpStatus.NOT_FOUND, "리뷰가 존재하지 않은 요청입니다."),
	NOT_FOUND_BOARD_REGION_DATA(HttpStatus.NOT_FOUND, "게시물이 존재하지 않은 지역입니다."),

	// [404 - FILE]
	NOT_FOUND_FILE_BOARD(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다."),
	NOT_FOUND_FILE_BOARDREVIEW(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다."),
	NOT_FOUND_FILE_USERPROFILEIMAGE(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다."),
	
	
	// [404 - PLANNER]
	NOT_FOUND_PLANNER_DATA(HttpStatus.NOT_FOUND, "여행기록이 없는 요청입니다."),

	
	// 409 [ 중복 데이터 존재 ]
	DUPLICATION(HttpStatus.CONFLICT, ""),
	// [409 - USER]
	DUPLICATION_USER_ID(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
	DUPLICATION_USER_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
	
	
	// 500 INTERNAL_SERVER_ERROR [ 서버 요청 처리 불가 ]
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리하지 못했습니다. 잠시후 다시 시도해주세요."),
	
	;
	
	private final HttpStatus httpStatus;
	private final String message;
}
