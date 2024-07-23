package com.daou.sabangnetserver.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private List<OrderResult> orderResults;
    private int totalCount;
    private int successCount;
    private int failCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResult {
        private Long orderNo;
        private boolean isSuccess;
    }
}





