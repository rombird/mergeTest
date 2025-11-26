package com.example.demo.service;

import com.example.demo.config.auth.exceptionHandler.ResourceNotFoundException;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.BoardFileEntity;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRoleType;
import com.example.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        Optional<User> opt = userRepository.findById(username);
        if(opt.isPresent()){
            String stored = opt.get().getPassword();
            System.out.println(">>> DB stored password for " + username + " : " + stored);
            System.out.println(">>> passwordEncoder.matches(raw, stored) => " + passwordEncoder.matches(password, stored));
        } else {
            System.out.println(">>> user not found in debug check");
        }



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
    // ##############################################
    // 회원정보 조회
    // ##############################################
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

    // ############################################
    // 회원정보 수정
    // @param username 현재 로그인된 사용자 ID
    // @param updateDto 수정할 이메일 및 연락처 정보
    // @return 수정된 사용자 응답 DTO
    // ##########################################

    @Transactional
    public UserResponseDto update(String username, UserUpdateDto updateDto){

        // 기존 회원 조회
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Entity의 수정 메서드를 사용하여 데이터 업데이트
        user.updateUser(updateDto.getEmail(), updateDto.getPhone());

        userRepository.save(user);

        // DTo로 변환하여 반환
        return UserResponseDto.fromEntity(user);
    }

    // ##############################################
    // 비밀번호 변경
    // @param username 현재 로그인된 사용자 ID
    // @param newPassword 새 비밀번호
    // @return 성공 여부 (true/false)
    // ##############################################
    @ Transactional
    public boolean updatePassword(String username, UserPasswordUpdateDto userPasswordUpdateDto){

        // 사용자 엔티티를 찾음
        User user = userRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // 새 비밀번호와 확인 필드 일치 여부 확인
        if(!userPasswordUpdateDto.getNewPassword().equals(userPasswordUpdateDto.getConfirmNewPassword())){
            throw new IllegalArgumentException("새 비밀번호오 확인 비밀번호가 일치하지 않습니다");
        }

        // 현재 비밀번호와 새 비밀번호 일치 여부 확인
        if(!passwordEncoder.matches(userPasswordUpdateDto.getCurrentPassword(), user.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호와 새 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호를 암호화하여 저장
        String encodedPassword = passwordEncoder.encode(userPasswordUpdateDto.getNewPassword());
        user.setPassword(encodedPassword);  // 암호화된 패스워드 userEntity에 저장

        userRepository.save(user);

        return true;
    }

    // ##################################################
    // 회원 탈퇴
    // ##################################################
    @jakarta.transaction.Transactional
    public void delete(String username, String password){
        // 사용자 엔티티 조회
        User user = userRepository.findById(username)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 비밀번호 일치 확인
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않아 회원탈퇴에 실패했습니다");
        }

        userRepository.deleteById(username);



    }


    // ################################################
    // Validate
    // ################################################
    public boolean validateAuthentication(){

        // 스프링 시큐리티의 현재 인증 정보(Authentication 객체)를 꺼냄
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        System.out.println("authentication: " + authentication);

        // 현재 사용자에게 부여된 권한 목록을 조회
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 현재 권한 목록 중에 ROLE_ANONYMOUS가 있는지 검사
        boolean isAnonymous = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"));

        // 인증된 상태에다 익명의 사용자가 아닌 객체 컨트롤러에 반환
        return authentication.isAuthenticated() && !isAnonymous;
    }



//    @jakarta.transaction.Transactional
//    public BoardDto update(BoardDto boardDto, List<MultipartFile> newFiles, List<Long> deleteFileIds) throws IOException {
//        // 1. 기존 게시글 조회
//        BoardEntity boardEntity = boardRepository.findById(boardDto.getId())
//                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
//
//        // 2. 비밀번호 검증
//        if (!boardEntity.getBoardPass().equals(boardDto.getBoardPass())){
//            // 비밀번호가 틀릴 경우 예외 발생
//            throw new IllegalArgumentException("게시글 수정 실패: 비밀번호가 일치하지 않습니다");
//        }
//
//        // 3. 텍스트 정보 업데이트 (제목, 내용)
//        // 불필요한 HTML 태그 제거
//        if (boardDto.getBoardContents() != null) {
//            String cleanText = Jsoup.clean(boardDto.getBoardContents(), Safelist.basicWithImages());
//
//            boardEntity.updateText(boardDto.getBoardTitle(), cleanText);
//        }
//
//        // 4. 파일 삭제 처리
//        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
//            for (Long fileId : deleteFileIds) {
//                // DB에서 파일 정보 조회
//                BoardFileEntity fileEntity = boardFileRepository.findById(fileId).orElse(null);
//
//                if (fileEntity != null) {   // 삭제하는 파일이 파일엔티티에 담겨있다면?
//                    // 로컬 디스크에서 파일 삭제
//                    String savePath = fileDir + fileEntity.getStoredFilename();
//                    File file = new File(savePath);
//                    if (file.exists()) {
//                        if(!file.delete()){
//                            log.error("파일 삭제 실패: {}", savePath);
//                        }
//                    }
//                    // DB에서 파일 데이터 삭제
//                    boardFileRepository.delete(fileEntity);
//                }
//            }
//        }
//
//        // 5. 새 파일 추가 처리
//        if (newFiles != null && !newFiles.isEmpty()) {
//            for (MultipartFile boardFile : newFiles) {
//                if (!boardFile.isEmpty()) {
//                    String originalFilename = boardFile.getOriginalFilename();
//                    String storedFilename = System.currentTimeMillis() + "_" + originalFilename;
//                    String savePath = fileDir + storedFilename;
//
//                    Long fileSize = boardFile.getSize();    // 파일 크기 가져오기
//
//                    boardFile.transferTo(new File(savePath));
//
//                    BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(boardEntity, originalFilename, storedFilename, fileSize);
//                    boardFileRepository.save(boardFileEntity);
//                }
//            }
//        }
//
//        // 6. 파일 첨부 여부(fileAttached) 상태 업데이트
//        // 현재 이 게시글에 연결된 파일 개수 확인
//        // 변경 사항이 즉시 DB에 반영되도록 saveAndFlush를 호출
//        // 삭제 및 추가된 파일 정보가 모두 DB에 담기기 위해서
//        boardRepository.saveAndFlush(boardEntity); // 변경 사항 즉시 반영 (혹시 모를 지연 처리 방지)
//
//        long currentFileCount = boardFileRepository.countByBoardEntityId(boardEntity.getId());
//
//        if(currentFileCount == 0){
//            boardEntity.updateFileAttached(0);
//        }else{
//            boardEntity.updateFileAttached(1);
//        }
//
//        return BoardDto.toBoardDto(boardEntity);
//    }










}