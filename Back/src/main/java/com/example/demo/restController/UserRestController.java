package com.example.demo.restController;

import com.example.demo.config.auth.jwt.JWTProperties; // JWT 상수 사용
import com.example.demo.domain.dto.TokenInfo;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name="UserController", description="This is User Controller")

public class UserRestController {

    private final UserService userService;

    // --- 1. 회원가입 (자체 로그인) ---
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto dto) {
        try {
            userService.addUser(dto);
            return new ResponseEntity<>("회원가입 성공", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("회원가입 중 서버 오류 발생", e);
            return new ResponseEntity<>("서버 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- 2. 로그인 (Access/Refresh Token 발행 및 쿠키 설정) ---
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto dto, HttpServletResponse response) {
        try {
            // 1. UserService를 통해 인증 및 Token 생성
            TokenInfo tokenInfo = userService.login(dto);

            // 2. Access Token을 HttpOnly 쿠키에 설정
            setAccessTokenCookie(response, tokenInfo.getAccessToken());

            // 3. Refresh Token을 HttpOnly 쿠키에 설정
            setRefreshTokenCookie(response, tokenInfo.getRefreshToken());

            // 4. 응답 본문은 토큰 정보 없이 성공 메시지만 전달 (토큰은 쿠키로 전달)
            return new ResponseEntity<>("로그인 성공 (토큰이 쿠키에 설정됨)", HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 또는 사용자 없음
            log.warn("로그인 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("로그인 중 서버 오류 발생", e);
            return new ResponseEntity<>("서버 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- 3. 로그아웃 (토큰 쿠키 제거 및 DB Refresh Token 삭제) ---
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody UserDto dto, HttpServletResponse response) {
        try {
            // 1. DB에서 Refresh Token 정보 삭제 (UserService에 구현된 deleteUser가 이 역할을 겸함)
            // 실제 로그아웃 로직은 SecurityConfig에서 Filter를 통해 처리되지만, 명시적으로 토큰을 삭제하는 메서드를 호출할 수도 있음
            userService.deleteUser(dto);

            // 2. Access Token 쿠키 삭제 (만료 시간을 0으로 설정)
            removeTokenCookie(response, JWTProperties.ACCESS_TOKEN_COOKIE_NAME);

            // 3. Refresh Token 쿠키 삭제
            removeTokenCookie(response, JWTProperties.REFRESH_TOKEN_COOKIE_NAME);

            return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);

        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return new ResponseEntity<>("로그아웃 처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ---------------------------------------------------------------------------------
    //  쿠키 헬퍼 메서드
    // ---------------------------------------------------------------------------------

    /**
     * Access Token을 HttpOnly 쿠키에 설정
     */
    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, token);
        cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000); // 초 단위로 설정
        cookie.setPath("/");
        cookie.setHttpOnly(true); // JavaScript 접근 방지 (보안 필수)
        // cookie.setSecure(true); // HTTPS 환경에서만 전송 (운영 환경시 필수)
        response.addCookie(cookie);
    }

    /*
     * Refresh Token을 HttpOnly 쿠키에 설정
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWTProperties.REFRESH_TOKEN_COOKIE_NAME, token);
        cookie.setMaxAge(JWTProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000); // 초 단위로 설정
        cookie.setPath("/");
        cookie.setHttpOnly(true); // JavaScript 접근 방지 (보안 필수)
        // cookie.setSecure(true); // HTTPS 환경에서만 전송 (운영 환경시 필수)
        response.addCookie(cookie);
    }

    /*
     * 지정된 이름의 토큰 쿠키를 제거
     */
    private void removeTokenCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0); // 즉시 만료
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}