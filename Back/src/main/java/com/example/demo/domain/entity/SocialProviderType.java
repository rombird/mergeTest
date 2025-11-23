package com.example.demo.domain.entity;

import lombok.Getter;

@Getter
public enum SocialProviderType {
    // NAVER, GOOGLE로 DB에 저장
    // 화면 출력용 - description(네이버, 구글)
    KAKAO("카카오"),
    NAVER("네이버"),
    GOOGLE("구글");

    private final String description;

    SocialProviderType(String description) {
        this.description = description;
    }
}
