package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.table.TableOrdersBaseDto;
import com.daou.sabangnetserver.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class TableController {

    @Autowired
    private TableService tableService;

    @GetMapping("/table")
    public ResponseEntity<List<TableOrdersBaseDto>> getPagenation(@RequestParam("currentPage") int currentPage) {
        List<TableOrdersBaseDto> ordersList = tableService.getPagenation(currentPage);

        // 로그에 페이지 호출 완료 메시지 출력
        log.info("{} 페이지 호출 완료", currentPage);
        return ResponseEntity.ok(ordersList);
    }
}
