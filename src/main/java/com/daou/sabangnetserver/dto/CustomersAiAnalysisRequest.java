package com.daou.sabangnetserver.dto;

import com.daou.sabangnetserver.model.CustomersAiAnalysis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomersAiAnalysisRequest {
    private String name;
    private List<String> orders;
}
