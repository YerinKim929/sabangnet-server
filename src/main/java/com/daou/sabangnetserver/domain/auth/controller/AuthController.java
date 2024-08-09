package com.daou.sabangnetserver.domain.auth.controller;

import com.daou.sabangnetserver.domain.auth.dto.ApproveRequestDto;
import com.daou.sabangnetserver.domain.auth.service.AuthService;
import com.daou.sabangnetserver.domain.user.service.UserService;
import com.daou.sabangnetserver.global.common.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/authority")
    public ResponseEntity<SuccessResponse> getAuthorityAndId() {

        return ResponseEntity.ok(
                SuccessResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("토큰에서 권한과 아이디 추출을 성공했습니다.")
                        .data(authService.extractIdAndAuthority())
                        .build()
        );
    }

    @PatchMapping("/update/approve")
    public ResponseEntity<SuccessResponse> approveByMaster(@Valid @RequestBody ApproveRequestDto requestDto) {
        userService.updateIsUsed(requestDto);
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("승인이 정상적으로 완료되었습니다.")
                .build());
    }

}
