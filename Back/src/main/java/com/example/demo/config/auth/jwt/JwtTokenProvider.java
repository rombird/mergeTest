package com.example.demo.config.auth.jwt;


<<<<<<< HEAD
import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.Signature;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.SignatureRepository;
import com.example.demo.domain.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
=======
import com.example.demo.domain.dto.TokenInfo;
import com.example.demo.domain.entity.Signature;
import com.example.demo.domain.repository.SignatureRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
>>>>>>> origin/막내
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
<<<<<<< HEAD
import org.springframework.security.core.userdetails.UserDetails;
=======
>>>>>>> origin/막내
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDate;
<<<<<<< HEAD
import java.util.*;
=======
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
>>>>>>> origin/막내
import java.util.stream.Collectors;

@Slf4j
@Component
<<<<<<< HEAD
public class JwtTokenProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SignatureRepository signatureRepository;
    //Key 저장
    private Key key;
    public void setKey(Key key){
        this.key = key;
    }
    public Key getKey(){
        return this.key;
    }

    //SIGNATURE 저장
    @PostConstruct
    public void init(){
        List<Signature> list = signatureRepository.findAll(); //1개 값만 저장되어있음
        if(list.isEmpty()){
            //처음 SIGNATURE발급
            byte[] keyBytes = KeyGenerator.getKeygen();
            this.key = Keys.hmacShaKeyFor(keyBytes);
=======
@RequiredArgsConstructor
public class JwtTokenProvider {
    // DB 연동을 위해 주입
    private final SignatureRepository signatureRepository;

    private Key key; // @PostConstruct에서 초기화하므로 final 제거

    // Filter에서 JWT 서명 키를 가져갈 수 있도록 getKey() 메서드 추가
    public Key getKey() {
        return key;
    }


    // @PostConstruct 초기화 메서드 (KeyGenerator + DB 관리)
    @PostConstruct
    public void init() {
        // 1. DB에서 기존 서명 키 조회
        List<Signature> list = signatureRepository.findAll();

        if (list.isEmpty()) {
            // 2. 키가 없으면 KeyGenerator로 난수 키 생성 후 DB에 저장
            byte[] keyBytes = KeyGenerator.keyGen(); // KeyGenerator 호출
            this.key = Keys.hmacShaKeyFor(keyBytes);

>>>>>>> origin/막내
            Signature signature = new Signature();
            signature.setKeyBytes(keyBytes);
            signature.setCreateAt(LocalDate.now());
            signatureRepository.save(signature);
<<<<<<< HEAD
            System.out.println("JwtTokenProvider init()  Key init : " + key);
        }else{
            //기존 SIGNATURE이용
            Signature signature = list.get(0);
            this.key = Keys.hmacShaKeyFor(signature.getKeyBytes());
            System.out.println("JwtTokenProvider init()  기존 Key 사용 : " + key);
        }
    }
    public JwtTokenProvider() {

    }

    // 유저 정보를 가지고 AccessToken, RefreshToken 을 생성하는 메서드
    public TokenInfo generateToken(Authentication authentication) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME); // 60초후 만료
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("username",authentication.getName()) //정보저장
                .claim("auth", authorities)//정보저장
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME))    //1일: 24 * 60 * 60 * 1000 = 86400000
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("JwtTokenProvider.generateToken.accessToken : " + accessToken);
        System.out.println("JwtTokenProvider.generateToken.refreshToken : " + refreshToken);

        return TokenInfo.builder()
                .grantType("Bearer")
=======
            log.warn("새로운 서명 키를 생성하여 DB에 저장했습니다. 이 키는 운영 환경에서 매우 중요합니다.");

        } else {
            // 3. 키가 있으면 기존 키를 로드하여 사용
            Signature signature = list.get(0);
            this.key = Keys.hmacShaKeyFor(signature.getKeyBytes());
            log.info("DB에서 기존 서명 키를 로드했습니다.");
        }
    }

    /**
     * Authentication 객체를 받아 Access Token과 Refresh Token을 생성
     */
    public TokenInfo generateToken(Authentication authentication) {
        // 권한 정보 가져오기(문자열로 반환)
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        //Access Token 만료시간
        Date accessTokenExpiresIn = new Date(now + JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME);

        // 1. Access Token 생성 (JWTProperties 사용)
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities) // 권한 정보 클레임
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256) //서버만 아는 key로 서명
                .compact(); //문자열 JWT로 변환


        //Refresh Token 만료시간
        Date refreshTokenExpiresIn = new Date(now + JWTProperties.REFRESH_TOKEN_EXPIRATION_TIME);

        // 2. Refresh Token 생성 (JWTProperties 사용)
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 3. AccessToken 과 RefreshToken을 TokenInfo 객체로 묶어 반환
        return TokenInfo.builder()
                .grantType("Bearer") //토큰 타입(Bearer)
>>>>>>> origin/막내
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

<<<<<<< HEAD

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }
        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(auth -> new SimpleGrantedAuthority(auth))
                        .collect(Collectors.toList());

        String username = claims.getSubject(); //username

        // PrincipalDetails 생성
        PrincipalDetails principalDetails = new PrincipalDetails();
        Optional<User> userOptional = userRepository.findById(username);
        UserDto userDto = null;
        if(userOptional.isPresent())
            userDto = UserDto.toDto(userOptional.get());
        principalDetails.setUserDto(userDto);

        System.out.println("JwtTokenProvider.getAuthentication UsernamePasswordAuthenticationToken : " + accessToken);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(principalDetails, "", authorities);
        return usernamePasswordAuthenticationToken;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) throws ExpiredJwtException{
=======
    //Access Token에서 사용자 인증 정보 추출(Filter에서 사용됨)
    public Authentication getAuthentication(String accessToken){

        Claims claims = parseClaims(accessToken);
        //auth 클레임 x == 권한 정보 없는 토큰 => 에러
        if (claims.get("auth") == null){
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }
        //문자열로 반환했던 권한들을 객체로 변환
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        //UsernamePasswordAuthenticationToken : 인증된 사용자 객체
        //credentials(null) -> 비밀번호는 필요 없음
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);

    }

    //토큰 유효성 검증
    public boolean validateToken(String token){
>>>>>>> origin/막내
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
<<<<<<< HEAD
            log.info("Invalid JWT Token", e);
        }
        catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            throw new ExpiredJwtException(null,null,null);

        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
=======
            log.info("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            // ExpiredJwtException 발생 시에도 parseClaims에서 클레임을 가져와야 하므로 여기서 true 반환하지 않음
            log.info("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.", e);
>>>>>>> origin/막내
        }
        return false;
    }

<<<<<<< HEAD
=======
    //만료된 토큰 클레임도 추출 가능
    private Claims parseClaims(String accessToken){
        try{
            //정상 토큰이면 claims 바로 반환
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        }catch(ExpiredJwtException e){
            //Refresh 로직을 위해 만료된 토큰 클레임 반환
            return e.getClaims();
        }
    }
>>>>>>> origin/막내
}