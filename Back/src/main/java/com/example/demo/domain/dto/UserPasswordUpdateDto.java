package com.example.demo.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserPasswordUpdateDto {

    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다")
    private String currentPassword;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,15}$",
            message = "비밀번호는 8~15자, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;

    private String confirmNewPassword;  // 새 비밀번호와 일치하는지
}
