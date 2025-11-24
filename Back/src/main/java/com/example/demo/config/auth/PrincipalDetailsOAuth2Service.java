package com.example.demo.config.auth;

import com.example.demo.config.auth.provider.GoogleUserInfo;
import com.example.demo.config.auth.provider.KakaoUserInfo;
import com.example.demo.config.auth.provider.NaverUserInfo;
import com.example.demo.config.auth.provider.OAuth2UserInfo;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRoleType;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class PrincipalDetailsOAuth2Service extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        //OAuth2UserInfo
        // 1. 소셜 로그인 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest); // 스프링 시큐리티가 소셜에서 받아온 엑세스 토큰을 이용해 사용자의 정보 가져옴
        System.out.println("oAuth2User : " + oAuth2User);
        System.out.println("getAttributes : " + oAuth2User.getAttributes());

        OAuth2UserInfo oAuth2UserInfo = null;
        //'kakao','naver','google','in-'
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String,Object> attributes = oAuth2User.getAttributes();

        // 카카오, 네이버, 구글 마다 정보를 주는 방식이 다르기 때문에 각각 다르게 처리
        // OAuth2UserInfo라는 인터페이스로 감싸서 어떤 로그인 방식이든 똑같이 데이터를 꺼낼 수 있도록
        if(provider.startsWith("kakao")) {
            //카카오 로그인시
            Long id = (Long)attributes.get("id");
            LocalDateTime connected_at = OffsetDateTime.parse( attributes.get("connected_at").toString() ).toLocalDateTime();
            Map<String,Object> properties = (Map<String,Object>)attributes.get("properties");
            Map<String,Object> kakao_account = (Map<String,Object>) attributes.get("kakao_account");
            System.out.println("id :" + id);
            System.out.println("connected_at :" + connected_at);
            System.out.println("properties :" + properties);
            System.out.println("kakao_account :" + kakao_account);
            oAuth2UserInfo = new KakaoUserInfo(id,connected_at,properties,kakao_account);

        }else if(provider.startsWith("naver")){
            //네이버 로그인시
            Map<String,Object> response = (Map<String,Object>)attributes.get("response");
            String id = (String)response.get("id");
            oAuth2UserInfo = new NaverUserInfo(id,response);

        }else if(provider.startsWith("google")){
            String id = (String)attributes.get("sub");
            oAuth2UserInfo = new GoogleUserInfo(id,attributes);
        }
        // 소셜 로그인시 확인용 출력
        System.out.println("oAuth2UserInfo : " + oAuth2UserInfo);


        // 최초 로그인시 로컬계정 DB 저장 처리
        String username = oAuth2UserInfo.getProvider()+"_"+oAuth2UserInfo.getProviderId(); // 소셜 로그인은 비번이 없으므로 우리 서버만의 username 생성해야함
        String password = passwordEncoder.encode("1234");
        Optional<User> userOptional =  userRepository.findById(username); // 사이트에 가입한 적 있는 지 확인(DB 조회)
        // UserDto 생성 (이유 : PrincipalDetails에 포함)
        // UserEntity 생성(이유 : 최초 로그인시 DB 저장용도)
        UserDto userDto =null;
        if(userOptional.isEmpty()){ // 처음 온 사람으로 UserDto를 만들고 roleType 을 USER로 설정한 뒤 DB에 저장
            //최초 로그인(Dto , Entity)
            userDto = UserDto   .builder()
                                .username(username)
                                .password(password)
                                .roleType(UserRoleType.USER) // 최초 로그인 -> 기본 권한을 주겠다는 의미
                                .build();
            User user = userDto.toEntity();
            userRepository.save(user);  //계정 등록
        }else{
            // 이미 가입된 회원으로 DB에서 정보를 꺼내오겠다 (Dto)
            User user = userOptional.get();
            userDto = UserDto.toDto(user);
        }

        // PrincipalDetails 전달
        // 스프링 시큐리티는 User Entity나 UserDto를 모르기 때문에 PrincipalDetail만 알아먹는다
        // -> UserDto와 소셜에서 받은 정보인 attributes 를 PrincipalDetails라는 곳에 넣어서 리턴
        PrincipalDetails principalDetails = new PrincipalDetails();
        userDto.setProvider(provider);
        userDto.setProviderId(oAuth2UserInfo.getProviderId());
        principalDetails.setUserDto(userDto);
        principalDetails.setAttributes(oAuth2User.getAttributes());
        principalDetails.setAccess_token(userRequest.getAccessToken().getTokenValue());
        return principalDetails;

    }
}
