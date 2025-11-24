package com.example.demo.domain.repository;

import com.example.demo.domain.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Entity 클래스만 받음
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // update board_table set board_hits=board_hits+1 where id=?
    // 게시글 조회수를 올리기 위한 repository에서 board_hits 하나씩 올리는 코드
    @Modifying
    @Query(value = "update BoardEntity b set b.boardHits=b.boardHits+1 where b.id=:id")
    void updateHits(@Param("id") Long id);

}
