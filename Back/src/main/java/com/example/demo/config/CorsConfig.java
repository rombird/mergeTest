package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 이 클래스를 설정 파일로 등록
public class CorsConfig implements WebMvcConfigurer {

   @Override
   public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**") // 모든 경로(엔드포인트)에 대해 CORS를 허용 (예: /api/board/paging)
               .allowedOrigins("http://localhost:3000") // React 개발 서버의 출처를 명시
               .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
               .allowedHeaders("*") // 모든 헤더 허용
               .allowCredentials(true) // 쿠키/인증 정보 전송 허용
               .maxAge(3600); // 캐시 기간 설정 (초 단위)
   }
}