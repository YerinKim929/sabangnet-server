package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.order.OrderApiResponse;
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
    public ResponseEntity<String> orderCollectData(
            @PathVariable int sellerNo,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy/MM/dd") String startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy/MM/dd") String endDate,
            @RequestParam("status") String status) {

        orderCollectService.fetchAndSaveOrders(sellerNo, startDate, endDate, status);

        return ResponseEntity.ok("Orders fetched and saved successfully.");
    }

}
