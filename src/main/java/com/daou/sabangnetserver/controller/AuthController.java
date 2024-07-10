package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.ProjectInfo;
import com.daou.sabangnetserver.dto.TokenRequestDto;
import com.daou.sabangnetserver.dto.TokenResponseDto;
import com.daou.sabangnetserver.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${apiKey.no}")
    private String apiKey;

    @Value("${sltnCd.no}")
    private String sltnCd;


    @PostMapping("/tokens/create")
    public ResponseEntity<TokenResponseDto> createTokens() {
        TokenRequestDto request = new TokenRequestDto(apiKey, sltnCd);
        TokenResponseDto tokenResponse = authService.createTokens(request);
        return ResponseEntity.ok(tokenResponse);
    }
}
