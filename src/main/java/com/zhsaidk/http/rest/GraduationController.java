package com.zhsaidk.http.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hello")
public class GraduationController {
    @GetMapping
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("Hello world");
    }
}
