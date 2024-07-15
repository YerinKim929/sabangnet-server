package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.order.OrderApiResponse;
import com.daou.sabangnetserver.service.OrderCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class OrderCollectController {

    @Autowired
    private OrderCollectService orderCollectService;

    @Value("${sltnCd.no}")
    private String sltnCd;

    @GetMapping("/order/{sellerNo}")
    public ResponseEntity<OrderApiResponse> orderCollectData(
            @PathVariable int sellerNo,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy/MM/dd") String startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy/MM/dd") String endDate,
            @RequestParam("status") String status) {

        ResponseEntity<OrderApiResponse> res = orderCollectService.fetchAndSaveOrders(sellerNo, startDate, endDate, status);

        return res;

    }
}
