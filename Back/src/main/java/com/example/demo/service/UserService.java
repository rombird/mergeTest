package com.example.demo.service;

import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.dto.UserResponseDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRoleType;
import com.example.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor // final 필드들을 주입하는 public 생성자를 lombok이 자동으로 생성
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            RedisUtil redisUtil){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisUtil = redisUtil;
    }

    // 자체 로그인 회원 가입 (존재 여부)
    @Transactional(readOnly = true)
    public Boolean existUser(UserDto dto) {
        return userRepository.existsByUsername(dto.getUsername());
    }


    // ###############################################
    // 회원가입
    // ###############################################
    //dto -> entity
    public User join(UserDto userDto){

        // 아이디 중복 검사
    if(userRepository.existsById(userDto.getUsername())){
        throw new IllegalArgumentException("이미 사용중인 아이디입니다. 회원가입이 불가능합니다");
    }

        User newUser = User.builder()
                .username(userDto.getUsername())
                .password( passwordEncoder.encode(userDto.getPassword()))   // 비밀번호 인코딩
                .isSocial(false) // 일반 가입
                .roleType(UserRoleType.USER)
                .name(userDto.getName())
                .phone(userDto.getPhone())
                .email(userDto.getEmail())
                .build();

        // save entity to DB
        return userRepository.save(newUser);

    }

    // ################################################
    // 로그인
    // ################################################
    // 사용자 인증을 시도하고 JWT 토큰을 발급하며, 리프레시 토큰을 Redis에 저장한다
    // username 사용자 ID
    // password 비밀번호
    // return 인증된 사용자의 이름과 발급된 토큰 정보를 담는 객체
    public LoginResult login(String username, String password) throws AuthenticationException {

        // 사용자 인증 시도
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password)
                );

        // JWT 토큰생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // Redis에 Refresh Token 저장
        // RT 접두사를 붙여 Redis 키 관리
        redisUtil.save("RT" + authentication.getName(), tokenInfo.getRefreshToken());

        // 결과를 반환하여 Controller가 Http 응답을 처리하도록 던짐
        return new LoginResult(authentication.getName(), tokenInfo);
    }


    public static class LoginResult{
        private final String username;
        private final TokenInfo tokenInfo;

        public LoginResult(String username, TokenInfo tokenInfo){
            this.username = username;
            this.tokenInfo = tokenInfo;
        }
        public String getUsername(){
            return username;
        }
        public TokenInfo getTokenInfo(){
            return tokenInfo;
        }

    }

    // 회원정보 조회
    // 인증된 사용자 이름을 기반으로 DB에서 사용자 정보를 조회하고 DTO로 변환하여 반환합니다
    // @Param -> username 현재 인증된 사용자의 이름
    // @return -> userResponseDto (사용자 이름, 권한)
    // @throws -> IllegalArgumentException DB에서 사용자를 찾지 못할 경우 발생
    @Transactional
    public UserResponseDto findUserInfoByUsername(String username){

        // DB 조회
        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자의 정보를 찾을 수 없습니다" + username));

        // Dto 변환 후 반환
        return UserResponseDto.fromEntity(user);
    }




}