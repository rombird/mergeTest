package com.example.demo.config.auth.jwt;

import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.repository.JwtTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenRepository jwtTokenRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        String accessToken = getAccessTokenFromCookie(request);

        if (accessToken != null) {
            try {
                // 1. Access Token 유효성 검증
                if (jwtTokenProvider.validateToken(accessToken)) {
                    // 유효: Authentication 생성 및 SecurityContext에 저장
                    setAuthentication(accessToken);
                }
            } catch (ExpiredJwtException e) {
                // 2. Access Token 만료: Refresh Token 확인 및 재발급 시도

                // ExpiredJwtException에서 subject(사용자 이름) 획득
                String expiredUsername = e.getClaims().getSubject();
                handleExpiredAccessToken(expiredUsername, response);

            } catch (Exception e) {
                log.error("Error processing Access Token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    //--------------------------------------------------------------------------------
    // Access Token 만료 및 재발급 처리
    //--------------------------------------------------------------------------------

    /**
     * Access Token이 만료되었을 때 Refresh Token을 확인하고 재발급
     */
    private void handleExpiredAccessToken(String expiredUsername, HttpServletResponse response) {
        log.info("AccessToken Expired. Attempting to renew token for user: {}", expiredUsername);

        // DB에서 토큰 정보 조회 (사용자 이름 기반)
        JwtToken entity = jwtTokenRepository.findByUsername(expiredUsername);

        if (entity != null) {
            try {
                // Refresh Token 유효성 검증
                if (jwtTokenProvider.validateToken(entity.getRefreshToken())) {

                    // Refresh Token 유효: Access Token 재발급
                    String newAccessToken = renewAccessToken(entity);

                    // 1. 쿠키로 새 Access Token 전달 (HttpOnly)
                    setAccessTokenCookie(response, newAccessToken);

                    // 2. DB의 Access Token 값 갱신
                    entity.setAccessToken(newAccessToken);
                    jwtTokenRepository.save(entity);

                    // 3. 재발급된 토큰으로 인증 처리
                    setAuthentication(newAccessToken);
                    log.info("Access Token successfully renewed and applied to Security Context.");
                }
            } catch (ExpiredJwtException e2) {
                // Refresh Token 만료: 토큰 제거 및 DB 기록 삭제 (재로그인 필요)
                log.warn("RefreshToken Expired. User needs to re-login.");
                removeAccessTokenCookie(response);
                jwtTokenRepository.deleteByUsername(expiredUsername);
            } catch (Exception exception) {
                log.error("Token renewal failed unexpectedly: {}", exception.getMessage());
            }
        }
    }

    /**
     * Access Token 재발급 로직 (JwtTokenProvider.renewAccessToken()과 유사하지만 필터 내에서 직접 구현)
     */
    private String renewAccessToken(JwtToken entity) {
        long now = (new Date()).getTime();

        return Jwts.builder()
                .setSubject(entity.getUsername())
                .setExpiration(new Date(now + JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256)
                .claim("auth", entity.getAuth())
                .compact();
    }

//--------------------------------------------------------------------------------
//  헬퍼 메서드 (Helper Methods)
//--------------------------------------------------------------------------------

    /**
     * Security Context에 Authentication을 저장
     */
    private void setAuthentication(String accessToken) {
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * 요청 쿠키에서 Access Token 값을 추출
     */
    private String getAccessTokenFromCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> JWTProperties.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst())
                .orElse(null);
    }


    /**
     * 응답에 Access Token 쿠키를 설정. (HttpOnly, 초 단위 MaxAge)
     */
    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        // 밀리초(ms)를 초(second)로 변환
        cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 보안을 위해 필수
        // cookie.setSecure(true); // HTTPS 환경에서만 활성화
        response.addCookie(cookie);
    }

    /**
     * 응답에서 Access Token 쿠키를 제거
     */
    private void removeAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}