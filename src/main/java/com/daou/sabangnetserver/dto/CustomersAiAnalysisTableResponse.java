package com.daou.sabangnetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomersAiAnalysisTableResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private boolean aiCollected;
}
