package com.example.demo.domain.service;

import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.domain.dto.CustomOAuth2User;
import com.example.demo.domain.dto.TokenInfo;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.dto.UserResponseDto;
import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.entity.SocialProviderType;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.entity.UserRoleType;
import com.example.demo.domain.repository.JwtTokenRepository;
import com.example.demo.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UserService extends DefaultOAuth2UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenRepository jwtTokenRepository;

    public UserService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       JwtTokenRepository jwtTokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtTokenRepository = jwtTokenRepository;
    }

    @Transactional(readOnly = true)
    public Boolean existUser(UserDto dto) {
        return userRepository.existsByUsername(dto.getUsername());
    }

    @Transactional
    public Long addUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 유저가 존재합니다.");
        }
        UserEntity entity = UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .isLock(false)
                .isSocial(false)
                .roleType(UserRoleType.USER)
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .build();

        return userRepository.save(entity).getId();
    }

    @Transactional
    public TokenInfo login(UserDto dto) {
        // 1. Username/Password 인증 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());

        // 2. 인증 시도 및 Authentication 객체 획득 (CustomUserDetailsService 호출됨)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. JWT 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 4. 기존 Refresh Token 삭제 및 새로운 토큰 저장
        jwtTokenRepository.deleteByUsername(dto.getUsername());

        // 사용자 권한 가져오기
        String authority = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException("권한 정보가 없습니다."));

        // JWT 토큰 엔티티 생성
        JwtToken jwtToken = JwtToken.builder()
                .username(dto.getUsername())
                .accessToken(tokenInfo.getAccessToken())
                .refreshToken(tokenInfo.getRefreshToken())
                .auth(authority)
                .build();

        jwtTokenRepository.save(jwtToken);

        return tokenInfo;
    }

    @Transactional
    public Long updateUser(UserDto dto) throws AccessDeniedException {
        String sessionUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!sessionUsername.equals(dto.getUsername())) {
            throw new AccessDeniedException("본인 계정만 수정 가능");
        }

        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(dto.getUsername(), false, false)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        entity.updateUser(dto);

        return userRepository.save(entity).getId();
    }

    @Transactional
    public void deleteUser(UserDto dto) throws AccessDeniedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String sessionUsername = auth.getName();
        String sessionRole = auth.getAuthorities().iterator().next().getAuthority();

        boolean isOwner = sessionUsername.equals(dto.getUsername());
        boolean isAdmin = sessionRole.equals("ROLE_" + UserRoleType.ADMIN.name());

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("본인 혹은 관리자만 삭제할 수 있습니다.");
        }

        userRepository.deleteByUsername(dto.getUsername());
        jwtTokenRepository.deleteByUsername(dto.getUsername());
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes;
        List<GrantedAuthority> authorities;
        String username;
        String role = UserRoleType.USER.name();
        String email;
        String nickname;
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        if (registrationId.equals(SocialProviderType.NAVER.name())) {
            attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            username = registrationId + "_" + attributes.get("id");
            email = attributes.get("email").toString();
            nickname = attributes.get("nickname").toString();
        } else if (registrationId.equals(SocialProviderType.GOOGLE.name())) {
            attributes = (Map<String, Object>) oAuth2User.getAttributes();
            username = registrationId + "_" + attributes.get("sub");
            email = attributes.get("email").toString();
            nickname = attributes.get("name").toString();
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        Optional<UserEntity> entity = userRepository.findByUsernameAndIsSocial(username, true);
        if (entity.isPresent()) {
            role = entity.get().getRoleType().name();
            UserDto dto = new UserDto();
            dto.setNickname(nickname);
            dto.setEmail(email);
            entity.get().updateUser(dto);
            userRepository.save(entity.get());
        } else {
            UserEntity newUserEntity = UserEntity.builder()
                    .username(username)
                    .password("")
                    .isLock(false)
                    .isSocial(true)
                    .socialProviderType(SocialProviderType.valueOf(registrationId))
                    .roleType(UserRoleType.USER)
                    .nickname(nickname)
                    .email(email)
                    .build();
            userRepository.save(newUserEntity);
        }

        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new CustomOAuth2User(attributes, authorities, username);
    }

    @Transactional(readOnly = true)
    public UserResponseDto readUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity entity = userRepository.findByUsernameAndIsLock(username, false)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다: " + username));

        return new UserResponseDto(username, entity.getIsSocial(), entity.getNickname(), entity.getEmail());
    }
}