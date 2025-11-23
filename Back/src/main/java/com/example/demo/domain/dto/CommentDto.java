package com.example.demo.domain.dto;

import com.example.demo.domain.entity.CommentEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
public class CommentDto {
    private Long id;    // 댓글 번호
    private String commentWriter;
    private String commentContents;
    private Long boardId;   // 게시글 번호
    private LocalDateTime commentCreatedTime;

    public static CommentDto toCommentDto(CommentEntity commentEntity, Long boardId) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(commentEntity.getId());
        commentDto.setCommentWriter(commentEntity.getCommentWriter());
        commentDto.setCommentContents(commentEntity.getCommentContents());
        commentDto.setCommentCreatedTime(commentEntity.getCreatedTime());
//        commentDto.setBoardId(commentEntity.getBoardEntity().getId()); -> 이거를 쓰고 싶다면 service에서 Transaction 설정 필수
        commentDto.setBoardId(boardId);
        return commentDto;
    }

}
