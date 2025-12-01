//
//package com.example.demo.controller;
//
//
//import com.example.demo.domain.dto.CommentDto;
//import com.example.demo.domain.repository.CommentRepository;
//import com.example.demo.service.CommentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/comment")
//public class CommentController {
//    private final CommentService commentService;
//
//
//    @PostMapping("/save")
//    public ResponseEntity save(@ModelAttribute CommentDto commentDto){    // commentDto에는 ajax로 보낸 데이터가 담긴다
//        System.out.println("commentDto" + commentDto);
//        Long saveResult = commentService.save(commentDto);
//        if(saveResult != null){
//            // 작성 성공하면 댓글 목록을 가져와서 리턴
//            // 댓글목록: 해당 게시글의 댓글 전체 -> 해당 게시글의 Id를 가지고 있는 댓글 전부 가져와야함
//            List<CommentDto> commentDtoList = commentService.findAll(commentDto.getBoardId());
//            return new ResponseEntity<>(commentDtoList, HttpStatus.OK); // 바디와 헤더를 같이 보낼 수 있는 ResponseEntity
//        }else{
//            return new ResponseEntity<>("해당 게시글이 존재하지 않습니다", HttpStatus.NOT_FOUND);
//        }
//
//    }
//
//}
