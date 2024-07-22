package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.order.OrderApiResponse;
import com.daou.sabangnetserver.dto.order.OrderRequestDto;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.service.OrderCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
public class OrderCollectController {

    @Autowired
    private OrderCollectService orderCollectService;

    @GetMapping("/order/{sellerNo}")
    public ResponseEntity<String> orderCollectData(@ModelAttribute OrderRequestDto orderRequestDto) {
        orderCollectService.fetchAndSaveOrders(orderRequestDto);
        LocalDate start = LocalDate.parse(orderRequestDto.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.parse(orderRequestDto.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String message = String.format("선택하신 날짜 %s부터 %s까지 주문 수집이 완료되었습니다.", start, end);
        return ResponseEntity.ok(message);
    }

}
