package com.springboot.MyTodoList.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloWorldController class
 * Simple controller to test the application
 */

@RestController
@RequestMapping("/test")
public class HelloWorldController {
    @GetMapping
    public String helloWorld() {
        return "Hello World!";
    }
}
