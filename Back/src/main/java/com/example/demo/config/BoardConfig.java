package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class BoardConfig implements WebMvcConfigurer {
    private String resourcePath = "/upload/**";     // view에서 접근할 경로
    private String savePath = "file:///C:/springboot_img";  // 실제 파일 저장 경로
    // 해석하면 "file:///C:/springboot_img"경로에 있는 파일에 "/upload/**"경로로 접근할거야

    @Value("${CKEditor.image}")
    private String CKEditorImageDir;

    public void addResourceHandlers(ResourceHandlerRegistry registry){

        // 게시판 일부 첨부파일 핸들러
        registry.addResourceHandler(resourcePath)
                .addResourceLocations(savePath);

        // CKEditor 이미지 핸들러
        // 브라우저 요청 http://localhost:8090/images/**
        // 서버 실제 경로: file:///C://springboot_img_upload/CKEditorImage
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + CKEditorImageDir);
    }

}
