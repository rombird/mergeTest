package com.example.demo.domain.repository;

import com.example.demo.domain.entity.BoardFileEntity;
import com.example.demo.domain.entity.NoticeFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeFileRepository extends JpaRepository<NoticeFileEntity, Long> {
    // select * from board_file_table where board_id = ?
    List<NoticeFileEntity> findAllByNoticeEntityId(Long noticeId);

    // 게시글에 첨부된 파일의 총 개수
    // JPA Naming Convention을 사용해서 자동으로 쿼리가 완성됨
    // select count(*) from board_file_table where board_id = ?
    long countByNoticeEntityId(Long noticeId);
}
