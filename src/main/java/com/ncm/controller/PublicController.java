package com.ncm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("public")
public class PublicController {

    @GetMapping("application-status")
    String status(){
        return "Application is running on port 8080";
    }

    @GetMapping("hello")
    String hello(){
        return "Hello World!";
    }
}
