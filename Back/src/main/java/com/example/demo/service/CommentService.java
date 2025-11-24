package com.example.demo.service;


import com.example.demo.domain.dto.CommentDto;
import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.CommentEntity;
import com.example.demo.domain.repository.BoardRepository;
import com.example.demo.domain.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // 댓글 저장
    public Long save(CommentDto commentDto){
        // 부모엔티티 조회
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(commentDto.getBoardId());

        if(optionalBoardEntity.isPresent()){

            BoardEntity boardEntity = optionalBoardEntity.get();
            CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDto, boardEntity);

            return commentRepository.save(commentEntity).getId();
        }else
        {
            return null;
        }

    }

    // 해당 게시글의 댓글 전부 가져오기
    public List<CommentDto> findAll(Long boardId) {
        // select * from comment_table where board_id=? order by id desc;
        BoardEntity boardEntity = boardRepository.findById(boardId).get();
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntityOrderByIdDesc(boardEntity);

        // EntityList -> DtoList
        List<CommentDto> commentDtoList = new ArrayList<>();
        for(CommentEntity commentEntity: commentEntityList){
            CommentDto commentDto = CommentDto.toCommentDto(commentEntity, boardId);
            commentDtoList.add(commentDto);
        }
        return commentDtoList;
    }
}
