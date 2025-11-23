package com.example.demo.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/user")
public class UserController {

    @GetMapping("/login")
    public String login(){
        log.info("get/ 로그인 페이지/ UserController");
        return "user/login";
    }

    @GetMapping("/join")
    public String join(){
        log.info("get/ 회원가입 페이지/ UserController");
        return "user/join";
    }

}
