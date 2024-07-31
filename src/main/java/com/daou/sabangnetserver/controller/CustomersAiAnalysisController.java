package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.CustomersAiAnalysisResponse;
import com.daou.sabangnetserver.model.CustomersAiAnalysis;
import com.daou.sabangnetserver.service.CustomersAiAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CustomersAiAnalysisController {

    @Autowired
    private CustomersAiAnalysisService customerAnalysisService;

    @GetMapping("/CustomersAiAnalysis")
    public List<CustomersAiAnalysisResponse> updateCustomerAnalysis() {
        List<CustomersAiAnalysis> analyses = customerAnalysisService.updateAndFetchAllCustomerAnalysis();
        return analyses.stream()
                .map(analysis -> new CustomersAiAnalysisResponse(
                        analysis.getId(),
                        analysis.getName(),
                        analysis.getPhoneNumber(),
                        analysis.isAiCollected()
                ))
                .collect(Collectors.toList());
    }
}