package com.daou.sabangnetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomersAiAnalysisResponse {
    private String name;
    private List<String> frequentOrders;
    private List<String> personalizedRecommendations;
    private String personalizedRecommendationsReason;
    private String customerSegments;
    private LocalDateTime analyzedTime;
}
