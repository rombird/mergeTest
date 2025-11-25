package com.example.demo.domain.dto;

import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRoleType;
import lombok.*;


@Getter
@Builder
public class UserResponseDto {
    private String username;
    private UserRoleType role;
    private String name;
    private String email;
    private String phone;
    private String password;

    // Entity -> DTO 변환 메서드
    public static UserResponseDto fromEntity(User user){
        return UserResponseDto.builder()
                .username(user.getUsername())
                .role(user.getRoleType())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .password(user.getPassword())
                .build();
    }
}
