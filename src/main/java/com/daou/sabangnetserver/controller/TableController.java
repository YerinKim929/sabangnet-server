package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.service.OrderCollectService;
import com.daou.sabangnetserver.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TableController {

    @Autowired
    private TableService tableService;

    @GetMapping("/table")
    public ResponseEntity<List<OrdersBase>> getPage(@RequestParam("currentPage") int currentPage) {
        List<OrdersBase> ordersList = tableService.getPagenation(currentPage);
        return ResponseEntity.ok(ordersList);
    }
}
