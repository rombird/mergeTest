package com.example.demo.config;
<<<<<<< HEAD
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
=======

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
>>>>>>> origin/조장
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

<<<<<<< HEAD
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
=======
    private static final String SECURITY_SCHME_NAME="BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                .addSecuritySchemes(SECURITY_SCHME_NAME, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                .bearerFormat("JWT")))
>>>>>>> origin/조장
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("API Title") // API의 제목
                .description("This is my Swagger UI") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/조장
