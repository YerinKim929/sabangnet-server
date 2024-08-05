package com.daou.sabangnetserver.controller;

import com.daou.sabangnetserver.dto.CustomersAiAnalysisResponse;
import com.daou.sabangnetserver.dto.CustomersAiAnalysisTableResponse;
import com.daou.sabangnetserver.dto.ProjectInfo;
import com.daou.sabangnetserver.model.CustomersAiAnalysis;
import com.daou.sabangnetserver.service.CustomersAiAnalysisService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class CustomersAiAnalysisController {

    @Autowired
    private CustomersAiAnalysisService customerAnalysisService;



    @GetMapping("/CustomersAiAnalysis")
    public Map<String, List<CustomersAiAnalysisTableResponse>> updateCustomerAnalysisTable() {
        return customerAnalysisService.updateAllCustomerAnalysisTable();
    }

    @GetMapping("/CustomersAiAnalysis/{customerId}")
    public ResponseEntity<CustomersAiAnalysisResponse> updateCustomerAnalysis(@PathVariable int customerId) {
        CustomersAiAnalysisResponse responses = customerAnalysisService.updateCustomerAnalysis(customerId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/CustomersAiAnalysis/force/{customerId}")
    public ResponseEntity<CustomersAiAnalysisResponse> updateForceCustomerAnalysis(@PathVariable int customerId) {
        CustomersAiAnalysisResponse responses = customerAnalysisService.forceUpdateCustomerAnalysis(customerId);
        return ResponseEntity.ok(responses);
    }

}

