package com.example.demo.config.auth.exceptionHandler;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice   // 예외가 발생하면 그 예외는 Controller 밖으로 던져지고 이 RestControllerAdvice가 받아먹는다
public class JoinExceptionHanlder {

    // 아이디 중복 처리
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBizException(IllegalArgumentException e){
        return Map.of("status", "fail", "message", e.getMessage());
    }

    // UserDTO @유효성 검증 실패 처리
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException e) {

        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return Map.of("status", "fail", "message", "defaultMessage");
    }

}
