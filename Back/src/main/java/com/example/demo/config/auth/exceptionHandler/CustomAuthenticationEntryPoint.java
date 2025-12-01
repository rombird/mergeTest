package com.example.demo.config.auth.exceptionHandler;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 인증 예외가 발생했을 때
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
//		log.error("CustomAuthenticationEntryPoint's commence invoke....");
//		response.sendRedirect("/login?error="+authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 UNAUTHORIZED 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String message = String.format("{\"status\": 401, \"message\": \"인증이 필요합니다. 로그인 페이지로 이동하세요.\"}");
        response.getWriter().write(message); // JSON 응답
	}

}







