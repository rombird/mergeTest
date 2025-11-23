package com.example.demo.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    //Swagger UI에서 JWT 사용하는 API 테스트를 가능하게 하기 위해 JWT 토큰을 입력할 수 있는 UI 기능 추가
    //사용할 보안 스키마 이름 정의
    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                //Authorization 헤더에 Bearer 토큰 첨부(API가 jwt를 사용하기에)
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("API Title") // API의 제목
                .description("This is my Swagger UI") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }
}