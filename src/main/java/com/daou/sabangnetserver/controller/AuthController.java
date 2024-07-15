package com.daou.sabangnetserver.controller;


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

    //현재 시각과 만료 시각 비교하여 토큰 자동 검증
    //토큰 갱신 및 발급 자동화
    @PostMapping("/tokens/{sellerNo}")
    public ResponseEntity<TokenResponseDto> autoValidateTokens(@PathVariable int sellerNo) {
        TokenRequestDto request = new TokenRequestDto(apiKey, sltnCd);
        TokenResponseDto tokenResponse = authService.validateAndRefreshTokens(request, sellerNo);
        return ResponseEntity.ok(tokenResponse);
    }
}
