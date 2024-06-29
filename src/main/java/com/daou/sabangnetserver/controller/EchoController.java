package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.ProjectInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/say")
@RestController
public class EchoController {

    @GetMapping("/hi")
    public ResponseEntity<String> echo() {
        return ResponseEntity.ok("Welcome to Daou Tech");
    }

    @GetMapping("/info")
    public ResponseEntity<ProjectInfo> echoInfo() {

        //HttpClientTest test = new HttpClientTest("http://www.google.co.kr"); // 통신 성공
        //HttpClientTest test = new HttpClientTest("https://www.google.co.kr"); // 통신 실패(SSL)

        return ResponseEntity.ok(new ProjectInfo());
    }
}
