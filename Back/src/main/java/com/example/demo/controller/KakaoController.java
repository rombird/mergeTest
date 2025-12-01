//package com.example.demo.controller;
//
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.ui.Model;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.Date;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/kakao")
//@Slf4j
//public class KakaoController {
//
////    @Value("${kakao.client-id}")
////    String clientId;
////    @Value("${kakao.redirect-uri}")
////    String redirectUri;
////    @Value("${kakao.client-secret}")
////    String clientSecret;
//    // http://localhost:8090/kakao/getCode - redirect uri
//    // key값을 properties에 넣는 것이 좋음 (전역변수로 설정)
//    private String CLIENT_ID="012b6de5ee32cd76b5f1aeb045b67200"; // REST API 키
//    private String REDIRECT_URI="http://localhost:8090/kakao/getCode";
//    private String LOGOUT_REDIRECT_URI="http://localhost:8099/kakao";
//
//    private String code; // code는 getCode에서
////    private KakaoTokenResponse kakaoTokenResponse; // 받은 response 보관 -> 여러가지로 꺼내서 써야하기 때문에
////    private KakaoFriendsResponse kakaoFriendsResponse; // 다른 함수에서 사용하기 위해 필드추가
//
//    // 인가코드 발급받는 것 자체가 로그인
//    @GetMapping("/login")
//    public String login(){
//        log.info("GET /kakao/login...");
//        return "redirect:https://kauth.kakao.com/oauth/authorize?client_id="+CLIENT_ID+"&redirect_uri="+REDIRECT_URI+"&response_type=code";
//    }
//
//    // login에서 code를 getCode에서 받아서 처리
//    @GetMapping("/getCode")
//    public String getCode(String code){
//        log.info("GET /kakao/getCode...code : "+code);
//        //getCode로 들어오면 보관을 해야지 다음 작업 계속 가능
//        this.code = code;
//        return "forward:/kakao/getAccessToken"; // forwarding을 해서 밑으로 내려보내주는 작업
//    }
//    // login, getCode 만들고 ->  http://localhost:8099/kakao/login 으로 들어가면 동의화면 뜨는데 거기서 동의하고 계속하기 누르면 getCode 출력
//
//    @GetMapping("/getAccessToken")
////    @ResponseBody // 비동기방식 - 페이지 처리 못하도록 (System.out 출력해서 볼때 에러 안뜨도록 -> 나중에 풀어줘야함)
//    // 동기방식으로 페이지 찾도록 responsebody 주석처리 -> String getAccessToken
//    public String getAccessToken(){
//        log.info("GET /kakao/getAccessToken...");
//
//        String url="https://kauth.kakao.com/oauth/token"; // token url 그대로 복붙
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        // 요청 헤더
//        HttpHeaders header = new HttpHeaders();
//        header.add("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
//
//        // 요청 바디 파라미터
//        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
//        params.add("grant_type","authorization_code");
//        params.add("client_id",CLIENT_ID);
//        params.add("redirect_uri",REDIRECT_URI);
//        params.add("code",code);
//
//        // paramete와 header를 하나의 entity로 묶어줌
//        HttpEntity< MultiValueMap<String,String>  > entity = new HttpEntity<>(params,header);
//
//        // 요청 후 응답 확인
////        ResponseEntity<KakaoTokenResponse> response =
////                restTemplate.exchange(url, HttpMethod.POST, entity, KakaoTokenResponse.class); // 요청방식 POST
////        System.out.println(response.getBody());
////        this.kakaoTokenResponse = response.getBody();
//
//        // main으로 리다이렉트
//        return "redirect:/kakao";
//        // login 해서 인증처리 했으면 getcode -> getAccessToken -> 토큰 받아서 처리 문제 없으면 다시 index page로
//
//    }
//    // kakao 까지만 경로 입력했을 때 뜨는 부분
//    // main으로 리다이렉트한 부분 받아서 처리
//    @GetMapping
//    public String main(Model model){ // 페이지에 실어 보내기 위해 model 추가
//        log.info("GET /kakao/index");
//
//        String url="https://kapi.kakao.com/v2/user/me"; // 사용자 정보 조회 url 그대로 복붙
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        // 요청 헤더
//        HttpHeaders header = new HttpHeaders();
////        header.add("Authorization","Bearer "+kakaoTokenResponse.getAccess_token()); // 공백 반드시! + 받은 엑세스 토큰불러오기
//        header.add("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
//        // 요청 바디 파라미터 필수X
//
//        // header를 하나의 entity로 묶어줌
//        HttpEntity entity = new HttpEntity(header);
//
//        // 요청 후 응답 확인
//        ResponseEntity<KakaoProfileResponse> response =
//                restTemplate.exchange(url, HttpMethod.POST, entity, KakaoProfileResponse.class); // 요청방식 POST
//        System.out.println(response.getBody());
//
//        // 뷰로 전달 - stringboot 에서만 사용하는 방법(프론트와 백을 나눠서 처리할꺼면 사용 x)
//        model.addAttribute("profile", response.getBody()); // -> index 파일에서 profile이라는 변수 사용가능
//        // index 파일에서 profile.properties 이런식으로 주려니까 너무 많아서 controller에서 작업
//        String nickname = response.getBody().getProperties().getNickname();
//        String image_url = response.getBody().getProperties().getThumbnail_image();
//        String email = response.getBody().getKakao_account().getEmail();
//
//        model.addAttribute("nickname", nickname);
//        model.addAttribute("image_url", image_url);
//        model.addAttribute("email", email);
//
//        return "kakao/index";
//    }
//
//
//    // 페이지 요청을 해야돼서 responsebody annotation 빼주기
//    @GetMapping("/logout")
//    public String logout(){
//        // 카카오계정과 함께 로그아웃 -> 실제 사용
//        log.info("GET /kakao/logout");
//
//        return "redirect:https://kauth.kakao.com/oauth/logout?client_id="+CLIENT_ID+"&logout_redirect_uri="+LOGOUT_REDIRECT_URI;
//        // logout3을 처리하면 카카오서버로 연결 -> 다시 메인으로 연결(redirect)
//    }
//
//    @Data
//    private static class KakaoAccount{
//        public boolean profile_nickname_needs_agreement;
//        public boolean profile_image_needs_agreement;
//        public Profile profile;
//        public boolean has_email;
//        public boolean email_needs_agreement;
//        public boolean is_email_valid;
//        public boolean is_email_verified;
//        public String email;
//    }
//
//    @Data
//    private static class Profile{
//        public String nickname;
//        public String thumbnail_image_url;
//        public String profile_image_url;
//        public boolean is_default_image;
//        public boolean is_default_nickname;
//    }
//
//    @Data
//    private static class Properties{
//        public String nickname;
//        public String profile_image;
//        public String thumbnail_image;
//    }
//
//    @Data
//    private static class KakaoProfileResponse{
//        public long id;
//        public Date connected_at;
//        public Properties properties;
//        public KakaoAccount kakao_account;
//    }
//
//
//}
