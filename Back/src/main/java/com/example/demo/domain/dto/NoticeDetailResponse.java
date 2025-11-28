package com.example.demo.domain.dto;
// 게시글 상세 정보와 댓글 목록을 함께 담아 클라이언트에 전달할 통합 응답 DTO

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class NoticeDetailResponse {
    // 게시글 상세 정보(기존 BoardDto를 사용)
    private NoticeDto noticeDto;

    // 댓글 목록
//    private List<CommentDto> commentDtoList;
}
