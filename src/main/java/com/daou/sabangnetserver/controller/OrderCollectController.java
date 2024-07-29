package com.daou.sabangnetserver.controller;


import com.daou.sabangnetserver.dto.order.OrderRequestDto;
import com.daou.sabangnetserver.dto.order.OrderResponseDto;
import com.daou.sabangnetserver.service.order.OrderCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@RestController
public class OrderCollectController {

    @Autowired
    private OrderCollectService orderCollectService;

    @GetMapping("/order/{sellerNo}")
    public ResponseEntity<OrderResponseDto> orderCollectData(@ModelAttribute OrderRequestDto orderRequestDto) {
        OrderResponseDto response = orderCollectService.fetchAndSaveOrders(orderRequestDto);
        LocalDate start = LocalDate.parse(orderRequestDto.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.parse(orderRequestDto.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return ResponseEntity.ok(response);
    }

}
