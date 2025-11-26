package com.example.demo.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {

//    @NotBlank(message = "이메일은 필수입니다")
//    @Email(message = "유효한 이메일 형식이 아닙니다")
//    private String Email;

    @NotBlank(message = "연락처는 필수입니다")
    @Pattern(regexp = "^[0-9]{11,13}$", message = "연락처는 11~13자리의 숫자만 가능합니다.")
    private String phone;

    private String email;
}
