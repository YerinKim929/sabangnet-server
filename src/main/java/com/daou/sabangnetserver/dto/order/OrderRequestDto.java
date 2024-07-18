package com.daou.sabangnetserver.dto.order;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class OrderRequestDto {

    private int sellerNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String endDate;

    private String status;
}

