package com.example.demo.config.auth.jwt;


import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.JwtTokenRepository;
import com.example.demo.domain.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;



@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    // JwtAuthorizationFilter : 여기에서 들어오는 모든 요청에 대해 사용자의 신분증(JWT)을 확인하는 관문 역할
    // 신분증(JWT)이 유효하면 세션에 등록, 신분증(JWT)이 만료되었으면 더 긴 RefreshToken을 확인해 새 신분(ACCESS TOKEN)을 재발급해주는 로직
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenRepository jwtTokenRepository;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException, IOException {
        System.out.println("[JWTAUTHORIZATIONFILTER] doFilterInternal...");

        // cookie 에서 JWT token을 가져옵니다.
        String token = null;
        String username = null;

        // 토큰 추가 : 요청 헤더나 쿠키에서 AccessToken과 Username을 가져옴
        try {       // JwtProperties.ACCESS_TOKEN_COOKIE_NAME : 액세스 토큰이 저장된 쿠키 이름(예: "access_token").
                    // username 쿠키도 같이 읽어옵니다(토큰에서 바로 꺼내지 않고 쿠키 username도 활용).
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME)).findFirst()
                    .map(cookie -> cookie.getValue())
                    .orElse(null);

            username = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("username") ).findFirst()
                    .map(cookie -> cookie.getValue())
                    .orElse(null);

        }catch(Exception e){

        }

        // 유효성 검증 : 토큰이 존재하면 jwtTokenProvider, validateToken을 통해 유효기간, 서명 등을 검사
        if (token != null && username!=null) {
            try {
                // 엑세스 토큰의 유효성체크
                // 토큰이 유효하면 토큰에서 사용자 정보를 추출해서 Spring Security의 세션(Security Context)에 저장 -> 인증된 사용자로 간주
                if(jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = getUsernamePasswordAuthenticationToken(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[JWTAUTHORIZATIONFILTER] : " + authentication);
                }

            } catch (ExpiredJwtException e)
            {   // 토큰만료시 예외처리(쿠키 제거) -> AccessToken이 만료되었다면 Redis에 저장된 Refresh Token을 확인하여 갱신 시도

                // REDIS REFRESH TOKEN
                String refreshToken =  redisUtil.getRefreshToken("RT:"+username);
                try{
                        if(jwtTokenProvider.validateToken(refreshToken)){ // refreshToken이 유효하면 AcccessToken을 발급하여 클라이언트에 전달
                            //accessToken 만료 o, refreshToken 만료 x -> access-token갱신
                            long now = (new Date()).getTime();
                            User user = userRepository.findByUsername(username);
                            // Access Token 생성
                            Date accessTokenExpiresIn = new Date(now + JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME); // 60초후 만료
                            String accessToken = Jwts.builder()
                                    .setSubject(username)
                                    .claim("username",username) //정보저장
                                    .claim("auth", user.getRoleType().name()) //정보저장
                                    .setExpiration(accessTokenExpiresIn)
                                    .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256)
                                    .compact();
                            // 클라이언트 전달
                            Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME,accessToken);
                            cookie.setMaxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                    }catch(ExpiredJwtException refreshTokenExpiredException){
                        // 토큰 완전 만료(RefreshToken 마저 완료 되면 Redish의 RefreshToken 모두 삭제하고 사용자는 재로그인
                        //엑세스토큰 만료 o , 리프레시 토큰 만료 o //클라이언트 만료된 AccessToken 삭제
                        Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME,null);
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                        //USERNAME쿠키도 삭제
                        Cookie userCookie = new Cookie("username",null);
                        userCookie.setMaxAge(0);
                        userCookie.setPath("/");
                        response.addCookie(userCookie);
                        //REDIS에서 삭제
                        redisUtil.delete("RT:"+username);
                }

                System.out.println("[JWTAUTHORIZATIONFILTER] : ...ExpiredJwtException ...."+e.getMessage());

            }catch(Exception e2){
                //그외 나머지
            }

        }
        chain.doFilter(request, response); // 필터 통과(인증/인가 처리 끝난 후 다음 필터나 최종 목저지인 Controller로 요청을 전달)
    }

    // TOKEN -> AUTHENTICATION 변환
    private Authentication getUsernamePasswordAuthenticationToken(String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        Optional<User> user = userRepository.findById(authentication.getName()); // 유저를 유저명으로 찾습니다.
        System.out.println("JwtAuthorizationFilter.getUsernamePasswordAuthenticationToken...authenticationToken : " +authentication );
        if(user.isPresent())
            return authentication;
        return null; // 유저가 없으면 NULL
    }

}