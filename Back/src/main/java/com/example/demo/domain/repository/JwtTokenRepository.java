package com.example.demo.domain.repository;

import com.example.demo.domain.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken,Long> {
    //AccessToken 을 받아 Entity 반환
    JwtToken findByAccessToken(String accessToken);

    //Username 을 받아 Entity 반환
    JwtToken findByUsername(String username);

    void deleteByAccessToken(String token);
}
=======
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {

    //UserService의 login 메서드에서 사용: 사용자명으로 Refresh Token 정보 조회
    JwtToken findByUsername(String username);

    // UserService의 deleteUser 메서드에서 사용: 사용자명으로 토큰 정보 삭제 (회원 탈퇴)
    @Transactional
    @Modifying
    @Query("delete from JwtToken t where t.username = ?1")
    void deleteByUsername(String username);
}
>>>>>>> origin/막내
