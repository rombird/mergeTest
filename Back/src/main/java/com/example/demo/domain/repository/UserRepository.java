package com.example.demo.domain.repository;

import com.example.demo.domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    User findByUsername(String username);

    // username이 존재하는지 확인
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // 자체 로그인 회원 정보 수정
    // 자체 로그인 여부 or 잠김여부(계정이 잠겨있는지 확인)
    Optional<User> findByUsernameAndIsSocial(String username, Boolean isSocial);

    @Transactional
    void deleteByUsername(String username);

}
