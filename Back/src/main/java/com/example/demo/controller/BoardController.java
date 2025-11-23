package com.example.demo.controller;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;


    // 게시글 목록 페이지
    @GetMapping("")
    public String findAll(Model model){
        // DB에서 전체 게시글 데이터를 가져와서 board.html에 보여준다
        log.info("GET /board 게시글 목록 페이지");

        List<BoardDto> boardDtoList =  boardService.findAll();
        model.addAttribute("boardList", boardDtoList);
        return "board";

    }

    // 글쓰기 페이지 이동
    @GetMapping("/writeBoard")
    public String writeForm(){
        log.info("Get/ writeBoard 게시판 글쓰기");

        return"writeBoard";
    }

    // 글쓴거 포스팅
    @PostMapping("/writeBoard")
    public String write(@ModelAttribute BoardDto boardDto){
        System.out.println("boardDto:" +boardDto);
        boardService.save(boardDto);

        return "redirect:/board";
    }

    // 게시글
    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model){
        // 해당 게시글의 조회수를 하나 늘리고
        // 게시글 데이터를 가져와서 detail.html에 출력
        boardService.updateHits(id);
        BoardDto boardDto = boardService.findById(id);
        model.addAttribute("board", boardDto);

        return "detail";
    }




}
