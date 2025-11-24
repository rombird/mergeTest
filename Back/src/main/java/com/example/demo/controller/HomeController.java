package com.example.demo.controller;

<<<<<<< HEAD
=======
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("")
public class HomeController {

    @GetMapping("")
    public String home(){
        log.info("Get / ...");
        return "main";
    }


    @GetMapping("/main")
    public String main(){
        log.info("Get / ...");
        return "main";
    }

<<<<<<< HEAD

=======
<<<<<<< HEAD

=======
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home(){
        log.info("GET /...");
        return "home";
    }
>>>>>>> origin/대장
=======
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)
}
