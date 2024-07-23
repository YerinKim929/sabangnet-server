package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.order.OrderApiResponse;
import com.daou.sabangnetserver.dto.order.OrderRequestDto;
import com.daou.sabangnetserver.dto.order.OrderResponseDto;
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
    public ResponseEntity<OrderResponseDto> orderCollectData(@ModelAttribute OrderRequestDto orderRequestDto) {
        OrderResponseDto response = orderCollectService.fetchAndSaveOrders(orderRequestDto);
        LocalDate start = LocalDate.parse(orderRequestDto.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.parse(orderRequestDto.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return ResponseEntity.ok(response);
    }

}
