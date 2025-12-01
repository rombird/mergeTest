package com.example.demo.domain.repository;

import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    // update notice_table set notice_hits=board_hits+1 where id=?
    // 게시글 조회수를 올리기 위한 repository에서 notice_hits 하나씩 올리는 코드
    @Modifying
    @Query(value = "update NoticeEntity n set n.noticeHits=n.noticeHits+1 where n.id=:id")
    void updateHits(@Param("id") Long id);
}