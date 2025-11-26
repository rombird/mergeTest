package com.example.demo.apiController;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.dto.UserPasswordUpdateDto;
import com.example.demo.domain.dto.UserResponseDto;
import com.example.demo.domain.dto.UserUpdateDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRoleType;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@Tag(name="UserController", description="This is User Controller")
public class apiUserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserService userService;

    public apiUserController(UserService userService){
        this.userService = userService;
    }


    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RedisUtil redisUtil;

//    @Operation(summary="join", description = "JOIN")
    @PostMapping(value = "/join", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> join_post(@Valid @RequestBody UserDto userDto){
        log.info("POST /join... 회원가입, UserRestController 유저이름은? {}" , userDto.getUsername());

        userService.join(userDto);

        // 성공 시 응답
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "회원가입 성공");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }



    // Header 방식 (Authorization: Bearer <token>)
    // - XXS 공격에 매우취약 - LocalStorage / SessionStorage에 저장시 문제 발생
    // - 쿠키방식이 비교적 안전
//    @Operation(summary="login", description = "LOGIN")
    @PostMapping(value = "/login" , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> login(@RequestBody UserDto userDto, HttpServletResponse resp) throws IOException {
        log.info("POST /login..." + userDto);                                       // resp : 쿠키를 주기 위한 용도
        Map<String, Object> response = new HashMap<>();

        try{
            UserService.LoginResult loginResult = userService.login(userDto.getUsername(), userDto.getPassword());

            response.put("state","success");
            response.put("message","인증성공!");

            //---------------------------------------------
            Cookie accessCookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME,
                                            loginResult.getTokenInfo().getAccessToken());
            accessCookie.setHttpOnly(true); // !!!!!!!!!!!!!!!!!!!!!!!!!! (중요) 쿠키에 관한 보안 처리 옵션(fn로 쿠키를 받았을 때 js에 접근 불허하는 옵션) !!!!!!!!!!!!!!!!!!!!!!!!!!
            accessCookie.setSecure(false); // Only for HTTPS : 가비아 도메인 사면 해당 옵션 풀어주기(접근차단 옵션이기때문에)
            accessCookie.setPath("/"); // Define valid paths
            accessCookie.setMaxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME); // 1 hour expiration

            // 사용자 이름 쿠키 설정(로그인 상태 확인 등 UI 용도)
            Cookie userCookie = new Cookie("username", loginResult.getUsername());
            userCookie.setHttpOnly(true);
            accessCookie.setSecure(false); // Only for HTTPS
            userCookie.setPath("/");
            userCookie.setMaxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME); // 7 days expiration

            // 응답에 쿠키 추가
            resp.addCookie(accessCookie);
            resp.addCookie(userCookie);

            // 최종 성공 응답 반환
            return new ResponseEntity<>(response, HttpStatus.OK);
            //---------------------------------------------
        } catch(AuthenticationException e) {
                // 인증 실패 처리 (AuthenticationManager가 예외를 던짐)
                System.out.println("인증실패, userRestController(login method) : " + e.getMessage());
                response.put("state", "fail");
                response.put("message", e.getMessage());
                return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);

        }
    }


    // ##################################################
    // 회원정보 조회하는 api주소
    // ##################################################
    @Operation(summary="user", description = "회원정보 조회")
    @GetMapping("/user")
    public ResponseEntity<UserResponseDto> findUser(Authentication authentication) {
        // 요청, 인증 정보 수집
        // Spring security가 인증에 실패하면 Controller에 도달하지 않고 401을 반환해야 한다
        String username = authentication.getName(); // 인증정보에 들어있는 name획득

        log.info("GET /findUser... 회원정보 조회, apiUserController, 넌 누구냐? {} ", authentication.getName());

        UserResponseDto userResponseDto = userService.findUserInfoByUsername(username);

        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

    // #########################################
    // 회원 정보 수정(이메일, 연락처)
    // #########################################

    @PutMapping("/myInfo/phone")
    public ResponseEntity<UserResponseDto> updateUserPhone(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateDto updateDto){

        String username = userDetails.getUsername();   // 로그인된 사용자 ID 획득
        UserResponseDto userResponseDto = userService.update(username, updateDto);  // 내용 수정

        return ResponseEntity.ok(userResponseDto);
    }

    // #########################################
    // 비밀번호 수정
    // #########################################
    @PutMapping("/myInfo/password")
    public ResponseEntity<String> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserPasswordUpdateDto userPasswordUpdateDto) {

        String username = userDetails.getUsername();

        // service로 DTO 전체를 전달
        userService.updatePassword(username, userPasswordUpdateDto);

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다");

    }

    // #########################################
    // 회원 탈퇴
    // #########################################

    @Operation(summary = "Delete /user", description = "회원탈퇴")
    @DeleteMapping("/user")
    public ResponseEntity<String> delete(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody Map<String, String> requestBody){

        // jwt에서 사용자 아이디 추출
        String username = principalDetails.getUsername();

        // 요청 본문
        String password = requestBody.get("password");

        if(password == null | password.isEmpty()){
            return ResponseEntity.badRequest().body("비밀번호를 입력해야 회원탈퇴가 가능합니다");
        }
        log.info("Delete /user ... 회원 탈퇴 요청, apiUserController, 누가? {}",username);

        try{
            userService.delete(username, password);
            return ResponseEntity.ok("회원 탈퇴 성공");
        }catch (IllegalArgumentException e){
            // 비밀번호 불일치 등의 예외 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


    // FN Login.jsx에서 토큰 유효성 검증과 관련
//    @Operation(summary="validate", description = "VALIDATE")
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(){

        if(userService.validateAuthentication()){
            System.out.println("인증된 상태입니다");
            return ResponseEntity.ok("");
        }
        System.out.println("미인증된 상태입니다");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");

    }




}
