package com.example.demo.config.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.domain.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalDetails implements UserDetails,OAuth2User {
    // PrincipalDetails : Local login(일반 로그인), Socail Login 사용자 모두 처리하기위해
    // -> 사용자의 모든 정보를 담은 보따리, 누구이고 어떤 권한을 가지고 있는지 증명하는 신분증 역할(일반 로그인, 소셜 로그인 두가지 신분증 역할)
    // UserDetails와 OAuth2User 두 인터페이스 동시 구현
    // UserDetails : 인증(로그인) 및 권한 확인
    // OAuth2User : 소셜 로그인 과정에서 받은 정보를 Security에 전달

	private UserDto userDto;
	public PrincipalDetails(UserDto userDto){

        this.userDto = userDto;
	}

	// OAuth2User
	Map<String, Object> attributes;
	String access_token;
	@Override
	public Map<String, Object> getAttributes() {
        return attributes;
    }

	@Override
	public String getName() {
        return userDto.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection <GrantedAuthority> authorities = new ArrayList();
		authorities.add(new SimpleGrantedAuthority(userDto.getRoleType().name()));
		return authorities;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return userDto.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return userDto.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
