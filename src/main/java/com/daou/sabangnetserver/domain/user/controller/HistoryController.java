package com.daou.sabangnetserver.domain.user.controller;

import com.daou.sabangnetserver.domain.user.dto.HistorySearchRequestDto;
import com.daou.sabangnetserver.domain.user.service.HistoryService;
import com.daou.sabangnetserver.global.common.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value="/")
@RestController
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/history")
    public ResponseEntity<SuccessResponse> searchHistory(@Valid @ModelAttribute HistorySearchRequestDto requestDto) {
        return ResponseEntity.ok(SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("히스토리를 성공적으로 조회했습니다.")
                .data(historyService.searchHistory(requestDto))
                .build());
    }
}