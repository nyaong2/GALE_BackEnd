package com.SideProject.GALE.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.SideProject.GALE.components.response.ResponseService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice	
/*
 * ControllerAdvice = @Controller 혹은 RestController 어노테이션으로 선언된아이의 에러를 핸들함.
 * ControllerAdvice 는 근본적으로 Controller에서만 작동하지만, Controller안에 Service의 메소드가 Throw 발생할 경우 그것또한 처리함.
*/
@RequiredArgsConstructor
public class ExceptionHandlerAdvice {
	private final ResponseService responseService;
	//private final Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);
	
	
	// [Custom Exception]
    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<?> CustomRuntimeExceptionHandler(CustomRuntimeException ex) 
    {
		return (ex.getMessage() == null) ? 
				responseService.Create(null, ex.getResCode(),null)
				: responseService.Create(null, ex.getResCode(), ex.getMessage());
    }
    
    @ExceptionHandler(CustomRuntimeException_Msg.class)
    public ResponseEntity<?> CustomRuntimeException_MsgHandler(CustomRuntimeException_Msg ex) 
    {
		return responseService.Create(null, ex.getResCode(), ex.getMessage());
    }
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> CustomExceptionHandler(CustomException ex) 
    {
		return (ex.getMessage() == null) ? 
				responseService.Create(null, ex.getResCode(),null)
				: responseService.Create(null, ex.getResCode(), ex.getMessage());
    }
    
    
    
    // [basic Exception]
    
//    @ExceptionHandler(HttpMessageNotReadableException.class) // controller에서 선언한 dto의 구조와 맞지 않는경우
//    public ResponseEntity<?> HttpMessageNotReadableEx_Handler() 
//    {
//		return responseService.Create(null, ResCode.BAD_REQUEST, "서버와 맞지 않는 구조의 요청입니다.");
//    } 
//    
//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class) // controller에서 선언한 dto의 구조와 맞지 않는경우
//    public ResponseEntity<?> HttpRequestMethodNotSupportEx_Handler()
//    {
//		return responseService.Create(null, ResCode.BAD_REQUEST, "서버에서 지원하지 않는 요청입니다.");
//    }    
}
