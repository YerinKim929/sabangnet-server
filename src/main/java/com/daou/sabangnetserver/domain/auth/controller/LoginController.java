package com.daou.sabangnetserver.domain.auth.controller;


import com.daou.sabangnetserver.domain.auth.dto.LoginRequestDto;
import com.daou.sabangnetserver.domain.auth.service.LoginService;
import com.daou.sabangnetserver.global.common.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value="/")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse> login(HttpServletRequest request, @Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("성공적으로 로그인되었습니다.")
                .data(loginService.validateLogin(request, loginRequestDto))
                .build());
    }
}
