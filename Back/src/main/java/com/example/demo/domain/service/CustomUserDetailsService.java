package com.example.demo.domain.service;

import com.example.demo.domain.dto.CustomUserDetails;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


//Security가 사용자 ID를 통해 DB에서 UserEntity를 조회하고 CustomUserDetails로 반환시키는 역할
//사용자 인증을 위해 사용자 이름(username)을 기반으로 DB에서 사용자 정보를 조회하고
//찾은 UserEntity를 CustomUserDetails 객체로 변환하여 반환
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // UserRepository 주입 (DB 접근)
    private final UserRepository userRepository;

    // Spring Security의 핵심 메서드: username으로 사용자 정보를 로드
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. DB에서 사용자 조회
        // JWT 로그인은 소셜 계정이 아니면서 (isSocial=false), 잠기지 않은 (isLock=false) 계정만 허용
        UserEntity userEntity = userRepository
                .findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 2. CustomUserDetails로 변환하여 반환
        return new CustomUserDetails(userEntity);
    }
}