package com.daou.sabangnetserver.domain.user.controller;

import com.daou.sabangnetserver.domain.user.dto.ProjectInfo;
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
        return ResponseEntity.ok(new ProjectInfo());
    }
}
