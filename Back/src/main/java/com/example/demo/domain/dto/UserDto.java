package com.example.demo.domain.dto;

import java.time.LocalDate;

import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    // Dto : 프론트엔드와 데이터를 주고받기 위한 껍데기
    // Entity : 원본 문서(바꾸면 X)

    // 1. 기본적인 유효성 검사 추가
    // 빈칸은 받지 않도록
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, message="비밀번호는 4자 이상이어야 합니다.")
    private String password;

    private String name;
    private String phone;
    private String email;
    private UserRoleType roleType;

	// OAUTH2 CLIENT INFO : 어떤 소셜인지 provider로
	private String provider;
	private String providerId;

	// DTO -> ENTITY : 사용자가 입력한 정보를 바탕으로 DB에 넣을 객체 생성
	public User toEntity(){
		return User.builder()
				.username(this.username)
				.password(this.password)
                .isSocial(false)
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
				.roleType(this.roleType != null ? this.roleType : UserRoleType.USER) // roleType이 null이면 기본값으로 USER을 주겠다.
				.build();
	}

	// ENTITY -> DTO : DB에서 꺼낸 정보를 화면에 뿌려주기 위해 변환
	public static UserDto toDto(User user){
		return UserDto.builder()
					.username(user.getUsername())
					.password(null) // 비밀번호는 보안상 화면으로 보내지 않도록 null 처리
                    .email(user.getEmail())
                    .phone(user.getPhone())
					.roleType(user.getRoleType())
					.build();
	}
}
