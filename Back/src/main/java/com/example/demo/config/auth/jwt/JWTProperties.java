package com.example.demo.config.auth.jwt;

public class JWTProperties {

    // Access Token 만료 시간 (보안을 위해 짧게 설정)
    // 1000 밀리초 * 60 초 * 30 분 = 1,800,000 ms (30분)
    public static final int ACCESS_TOKEN_EXPIRATION_TIME=1000*60*30;

    // Refresh Token 만료 시간 (Access Token보다 길게 설정)
    // 1000 ms * 60 s * 60 m * 24 h * 7 d = 604,800,000 ms (7일)
    public static final int REFRESH_TOKEN_EXPIRATION_TIME=1000*60*60*24*7;

    // 쿠키 이름 정의
    public static final String ACCESS_TOKEN_COOKIE_NAME="access-token";
    public static final String REFRESH_TOKEN_COOKIE_NAME="refresh-token";

    /* * Access Token의 쿠키 만료 시간 설정:
     * Access Token은 짧게 만료되더라도, Refresh Token을 통해 재발급을 시도해야 하므로
     * 쿠키 자체는 Refresh Token 만료 시간과 동일하게 유지하는 것이 일반적
     */
    public static final int ACCESS_TOKEN_COOKIE_EXPIRATION_TIME=REFRESH_TOKEN_EXPIRATION_TIME;

    // JWT 토큰 타입 (Bearer)
    public static final String TOKEN_PREFIX="Bearer ";

    // 권한 클레임 키 이름
    public static final String AUTHORITIES_KEY="auth";
}