package com.example.demo.domain.repository;

import com.example.demo.domain.entity.BoardFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardFileRepository extends JpaRepository<BoardFileEntity, Long> {
    // select * from board_file_table where board_id = ?
    List<BoardFileEntity> findAllByBoardEntityId(Long boardId);

}
