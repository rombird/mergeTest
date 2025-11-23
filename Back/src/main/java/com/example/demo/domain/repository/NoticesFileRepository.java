package com.example.demo.domain.repository;

import com.example.demo.domain.entity.NoticesEntity;
import com.example.demo.domain.entity.NoticesFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticesFileRepository extends JpaRepository<NoticesFile, Long> {
    // JPA 관례를 따라 NoticesEntity 객체를 인자로 받아 연결된 모든 파일을 조회
    // NoticesFileService의 deleteFilesByNotices(NoticesEntity notices)에서 사용
    List<NoticesFile> findByNotices(NoticesEntity notices);
}