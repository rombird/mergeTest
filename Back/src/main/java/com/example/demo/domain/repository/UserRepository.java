package com.example.demo.domain.repository;

<<<<<<< HEAD
import com.example.demo.domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
=======
import com.example.demo.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
>>>>>>> origin/막내
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

<<<<<<< HEAD
@Repository
public interface UserRepository extends JpaRepository<User,String> {
    User findByUsername(String username);
=======
public interface UserRepository extends JpaRepository<UserEntity, Long> {
>>>>>>> origin/막내

    // username이 존재하는지 확인
    Boolean existsByUsername(String username);

    // 자체 로그인 회원 정보 수정
    // 자체 로그인 여부 or 잠김여부(계정이 잠겨있는지 확인)
<<<<<<< HEAD
    Optional<User> findByUsernameAndIsLockAndIsSocial(String username, Boolean isLock, Boolean isSocial);

    // Oauth2 UserService
    Optional<User> findByUsernameAndIsSocial(String username, Boolean social);

    Optional<User> findByUsernameAndIsLock(String username, Boolean isLock);

    @Transactional
    void deleteByUsername(String username);

}
=======
    Optional<UserEntity> findByUsernameAndIsLockAndIsSocial(String username, Boolean isLock, Boolean isSocial);

    // Oauth2 UserService
    Optional<UserEntity> findByUsernameAndIsSocial(String username, Boolean social);

    Optional<UserEntity> findByUsernameAndIsLock(String username, Boolean isLock);

    @Transactional
    void deleteByUsername(String username);
}
>>>>>>> origin/막내
