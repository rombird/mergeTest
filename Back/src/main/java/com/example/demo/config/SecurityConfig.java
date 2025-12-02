package com.example.demo.config;


import com.example.demo.config.auth.exceptionHandler.CustomAccessDeniedHandler;
import com.example.demo.config.auth.exceptionHandler.CustomAuthenticationEntryPoint;
import com.example.demo.config.auth.jwt.JwtAuthorizationFilter;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.loginHandler.CustomLoginFailureHandler;
import com.example.demo.config.auth.loginHandler.CustomLoginSuccessHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutSuccessHandler;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.repository.JwtTokenRepository;
import com.example.demo.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;


// 1. CORS 허용
// 2. CSRF 비활성화
// 3. Swagger, 로그인, 회원가입 허용
// 4. 나머지 요청은 인증 필요
// 5. 세션 사용 안 함 -> jwo 방식
// 6. 요청마다 jwt 필터에서 토큰 인증
// 7. 실패 시 Custom EntryPoint / AccessDeniedHandler 동작
// 8. OAuth2 로그인 허용
// 9. 로그아웃 커스텀 처리



@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomLoginSuccessHandler customLoginSuccessHandler;
	@Autowired
	private CustomLogoutHandler customLogoutHandler;
	@Autowired
	private CustomLogoutSuccessHandler customLogoutSuccessHandler;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private JwtTokenRepository jwtTokenRepository;
	@Autowired
	private RedisUtil redisUtil;


	@Bean
	protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
		//CSRF비활성화
		http.csrf((config)->{config.disable();});
		//CSRF토큰 쿠키형태로 전달
//		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
		//권한체크
		http.authorizeHttpRequests((auth)->{
			auth.requestMatchers("/",
                                        "/join",
                                        "/login", // 인증 없이 접근 허용
                                        "/validate",
                                        "/oauth2/**",
                                        "/login/oauth2/**",
                                        // 게시판 관련 로그인 없어도 볼 수 있는 것들
                                        "/api/board/paging",
                                        "/api/board/*",
                                        "/api/board/download/**"
            ).permitAll();

            // 유저관련 로그인 해야지 가능
            auth.requestMatchers("/myInfo/phone").authenticated();
            auth.requestMatchers("/user").authenticated();
            auth.requestMatchers("/myInfo/password").authenticated();

            // 크롤링 관련, 누구나 열람 가능
            auth.requestMatchers("/api/crawl/**").permitAll();

            // 챗봇 관련
            // 심플 챗봇, 누구든지 가능
            auth.requestMatchers("/api/v1/simple-chat").permitAll();

            // 게시판 API 권한 설정
            // 로그인 없어도 OK
            auth.requestMatchers("/api/board/paging").permitAll();  // 게시판 조회
            auth.requestMatchers("/api/board/{id}").permitAll();    // 상세 조회
            auth.requestMatchers("/api/board/download/**").permitAll(); // 첨부파일 다운로드

            // 게시판, 로그인 해야지 가능
            auth.requestMatchers("/api/board/WriteBoard").authenticated();   // 글 작성
            auth.requestMatchers("/api/board/update/**").authenticated();   // 글 수정
            auth.requestMatchers("/api/board/delete/**").permitAll();   // 글 삭제
            auth.requestMatchers("/api/board/image/upload").authenticated();    // CKEditor 텍스트
            auth.requestMatchers("/api/comment/save").authenticated(); //

            // 차트 보는 거 로그인 허용할까 말까
            auth.requestMatchers("/api/sales/summary").permitAll();


            // 공지사항 보는 거 로그인 안해도 가능
            auth.requestMatchers("/api/notice/paging").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/api/notice/*").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/api/notice/download/**").permitAll();

            // 공지사항(관리자만 가능), 글 쓰기와 수정 삭제는 관리자만 가능
            auth.requestMatchers("/api/notice/save").hasAuthority("ADMIN");
            auth.requestMatchers(HttpMethod.PUT, "/api/notice/update/*").hasAuthority("ADMIN");
            auth.requestMatchers(HttpMethod.DELETE, "/api/notice/delete/*").permitAll();    //


            // 2. Swagger 관련 경로 전체 허용 추가!
            auth.requestMatchers(
                    "/v3/api-docs",                // v3/api-docs 경로 (JSON)
                    "/v3/api-docs/**",             // v3/api-docs 이하 모든 경로 (JSON)
                    "/swagger-ui.html",            // 기본 UI HTML 파일
                    "/swagger-ui/**"          // Swagger UI 내부 리소스 (JS, CSS, Images)
            ).permitAll();

            // 내가 주석처리함 user 경로를 이미 사용중이기 때문에
			auth.anyRequest().authenticated();
		});

		//-----------------------------------------------------
		// [수정] 로그인(직접처리 - UserRestController)
        // 리액트에서 넘길거기때문에 disable설정이면 된다

        // 기본 로그인 폼 및 처리 필터를 사용하지않겠다(JWT 방식을 사용하므로 올바른 설정입니다)

		//-----------------------------------------------------
		http.formLogin((login)->{
			login.disable();
//            login.permitAll();
//            login.loginPage("/login");
//            login.successHandler(customLoginSuccessHandler());
//            login.failureHandler(new CustomAuthenticationFailureHandler());
		});


        // 로그아웃 시 해야할 작업들
        // AccessToken 블랙리스트 등록(Redis)
        // RefreshToken 삭제
        // 상태 로그 저장
		http.logout((logout)->{
			logout.permitAll();
			logout.addLogoutHandler(customLogoutHandler);
			logout.logoutSuccessHandler(customLogoutSuccessHandler);
		});
		//예외처리
		http.exceptionHandling((ex)->{
			ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
			ex.accessDeniedHandler(new CustomAccessDeniedHandler());
		});

		//OAUTH2 로그인
		http.oauth2Login((oauth2)->{
			oauth2.loginPage("/login");
		});

		// 세션 사용 안함
		http.sessionManagement((sessionManagerConfigure)->{
			sessionManagerConfigure.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		});

		//JWT FILTER 추가
		http.addFilterBefore(new JwtAuthorizationFilter(userRepository,jwtTokenProvider,jwtTokenRepository,redisUtil), LogoutFilter.class);
		//-----------------------------------------------
		//[추가] CORS
		//-----------------------------------------------
		http.cors((config)->{
			config.configurationSource(corsConfigurationSource());
		});

		return http.build();
		
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	//-----------------------------------------------------
	//[추가] CORS - Bean을 생성해서 반환형태로
	//-----------------------------------------------------
	@Bean
	CorsConfigurationSource corsConfigurationSource(){
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedHeaders(Collections.singletonList("*")); //허용헤더
		config.setAllowedMethods(Collections.singletonList("*")); //허용메서드
		config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:3000"));  //허용도메인
		config.setAllowCredentials(true); // COOKIE TOKEN OPTION
		return new CorsConfigurationSource(){
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				return config;
			}
		};
	}
	//-----------------------------------------------------
	//[추가] ATHENTICATION MANAGER 설정 - 로그인 직접처리를 위한 BEAN
    // authentication을 빈으로 만들어 반환해야지 로그인처리 직접 가능
	//-----------------------------------------------------
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
