package com.example.demo.service;

import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRoleType;
import com.example.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor // final 필드들을 주입하는 public 생성자를 lombok이 자동으로 생성
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 자체 로그인 회원 가입 (존재 여부)
    @Transactional(readOnly = true)
    public Boolean existUser(UserDto dto) {
        return userRepository.existsByUsername(dto.getUsername());
    }

    // 자체 로그인 회원 가입 - username, email, name, password 받을 것
//    @Transactional
//    public void join(UserDto userDto){
//        log.info(userDto.getUsername(), UserRoleType.USER.name());
//
//        User user = User.builder()
//                .username(userDto.getUsername())
//                .password(passwordEncoder.encode(userDto.getPassword())) // Dto에서 넘어온 비밀번호 인코딩
//                .isLock(false)
//                .isSocial(false)
//                .socialProviderType(null)
//                .roleType(UserRoleType.USER)
////                .name(userDto.getName())
//                .phone(userDto.getPhone())
//                .email(userDto.getEmail())
//                .build();
//
//        // DB 저장
//        userRepository.save(user);
//    }

    // 자체 로그인

    // 자체 로그인 회원 정보 수정
    // 자체 로그인 여부 or 잠김여부(계정이 잠겨있는지 확인)

    // 자체/소셜 로그인 회원 탈퇴

    // 소셜 로그인 (매 로그인시 : 신규 = 가입, 기존 = 업데이트)

    // 자체/소셜 유저 정보 조회


}
