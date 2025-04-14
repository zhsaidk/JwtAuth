package com.zhsaidk.http.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String getHelloMessage(){
        return "Hello World";
    }

    @GetMapping("/bye")
    public String getByeMessage(){
        return "Bye bye";
    }
}
