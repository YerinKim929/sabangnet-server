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

import java.util.List;


@RestController
public class OrderCollectController {

    @Autowired
    private OrderCollectService orderCollectService;

    @GetMapping("/order/{sellerNo}")
    public ResponseEntity<String> orderCollectData(@ModelAttribute OrderRequestDto orderRequestDto) {
        orderCollectService.fetchAndSaveOrders(
                orderRequestDto
        );

        return ResponseEntity.ok("Orders fetched and saved successfully.");
    }

}
