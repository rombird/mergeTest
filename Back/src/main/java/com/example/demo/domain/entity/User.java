package com.example.demo.domain.entity;

import com.example.demo.domain.dto.UserDto;
import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class) // 엔티티가 저장되거나 업데이트될때 현재시간이나 로그인한 사용자 Id 자동 주입
public class User {

    @Id
    @Column(name = "username", unique = true, nullable = false, updatable = false)
    private String username; // 아이디

    @Column(name = "password", nullable = false)
    private String password; // 비밀번호

    @Column(name = "is_social", nullable = false)
    private Boolean isSocial = false;

    @Enumerated(EnumType.STRING) // USER, ADMIN 문자열 그대로 저장
    @Column(name = "social_provider_type")
    private SocialProviderType socialProviderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    @Builder.Default
    private UserRoleType roleType = UserRoleType.USER; // 기본값을 USER로 설정

    @Column(name = "name")
    private String name;

    @Column(name ="phone")
    private String phone;

    @Column(name = "email")
    private String email;

//    @Column(name = "is_lock", nullable = false)
//    private Boolean isLock = false;

//    @CreatedDate
//    @Column(name = "created_date", updatable = false)
//    private LocalDateTime createdDate;
//
//    @LastModifiedDate
//    @Column(name = "updated_date")
//    private LocalDateTime updatedDate;

    // 수정가능한 항목 - email, phone
    public void updateUser(String newEmail, String newPhone) {
        this.email = newEmail;
        this.phone = newPhone;
    }
}