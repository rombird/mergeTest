package com.example.demo.apiController;

import com.example.demo.domain.dto.CommentDto;
import com.example.demo.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
@Slf4j
public class apiCommentController {
    private final CommentService commentService;

    // 댓글 저장
    @PostMapping("/save")
    public ResponseEntity<List<CommentDto>> save(@RequestBody CommentDto commentDto){
        log.info("post / api/comment/save.. 댓글 작성 요청: {}, apiCommentController", commentDto);

        Long saveResult = commentService.save(commentDto);

        if(saveResult != null){
            // 작성 성공 후, 갱신된 댓글 목록을 가져와서 리턴
            List<CommentDto> commentDtoList = commentService.findAll(commentDto.getBoardId());

            // HTTP 상태 코드 200 ok와 함꼐 댓글 목록 반환
            return new ResponseEntity<>(commentDtoList, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
