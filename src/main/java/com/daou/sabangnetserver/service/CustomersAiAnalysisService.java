package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.CustomersAiAnalysisRequest;
import com.daou.sabangnetserver.dto.CustomersAiAnalysisResponse;
import com.daou.sabangnetserver.dto.CustomersAiAnalysisTableResponse;
import com.daou.sabangnetserver.model.CustomersAiAnalysis;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.repository.CustomersAiAnalysisRepository;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomersAiAnalysisService {

    @Autowired
    private CustomersAiAnalysisRepository customersAiAnalysisRepository;

    @Autowired
    private OrdersBaseRepository ordersBaseRepository;

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Transactional
    public Map<String, List<CustomersAiAnalysisTableResponse>> updateAllCustomerAnalysisTable() {
        List<OrdersBase> ordersBases = ordersBaseRepository.findAll();

        for (OrdersBase ordersBase : ordersBases) {
            String name = ordersBase.getRcvrNm();
            String phoneNumber = ordersBase.getRcvrMphnNo();
            String ordNo = ordersBase.getOrdNo();

            Optional<CustomersAiAnalysis> existingAnalysisOpt = customersAiAnalysisRepository.findByNameAndPhoneNumber(name, phoneNumber);

            List<OrdersDetail> ordersDetails = ordersBase.getOrdersDetail();
            List<CustomersAiAnalysis.PurchaseInfo> purchaseInfoList = ordersDetails.stream()
                    .map(detail -> CustomersAiAnalysis.PurchaseInfo.builder()
                            .ordNo(ordNo)
                            .productName(detail.getPrdNm())
                            .optionValue(detail.getOptVal())
                            .build())
                    .collect(Collectors.toList());

            if (existingAnalysisOpt.isEmpty()) {
                // 새로운 고객 데이터 생성
                CustomersAiAnalysis analysis = CustomersAiAnalysis.builder()
                        .name(name)
                        .phoneNumber(phoneNumber)
                        .personalizedRecommendations(null)// 기본값으로 false 설정, 필요한 경우 변경 가능
                        .purchaseInfo(purchaseInfoList)
                        .build();

                customersAiAnalysisRepository.save(analysis);
            } else {
                // 기존 고객 데이터에 새로운 주문 추가
                CustomersAiAnalysis analysis = existingAnalysisOpt.get();
                for (CustomersAiAnalysis.PurchaseInfo newInfo : purchaseInfoList) {
                    boolean exists = analysis.getPurchaseInfo().stream().anyMatch(existingInfo ->
                            existingInfo.getOrdNo().equals(newInfo.getOrdNo()) &&
                                    existingInfo.getProductName().equals(newInfo.getProductName()) &&
                                    existingInfo.getOptionValue().equals(newInfo.getOptionValue())
                    );

                    if (!exists) {
                        analysis.getPurchaseInfo().add(newInfo);
                    }
                }
                customersAiAnalysisRepository.save(analysis);
            }
        }


        List<CustomersAiAnalysis> analyses = customersAiAnalysisRepository.findAll();
        List<CustomersAiAnalysisTableResponse> responseList = analyses.stream()
                .map(analysis -> new CustomersAiAnalysisTableResponse(
                        analysis.getId(),
                        analysis.getName(),
                        analysis.getPhoneNumber(),
                        analysis.getPersonalizedRecommendations(),
                        analysis.getPurchaseInfo().size(), // orderCount 설정
                        analysis.getAnalyzedTime() // analyzedTime 설정
                ))
                .collect(Collectors.toList());

        Map<String, List<CustomersAiAnalysisTableResponse>> responseMap = new HashMap<>();
        responseMap.put("orders", responseList);
        return responseMap;
    }

    @Transactional
    public CustomersAiAnalysisResponse updateCustomerAnalysis(int customerId) {
        // 데이터베이스에서 고객 정보 조회
        Optional<CustomersAiAnalysis> customerOpt = customersAiAnalysisRepository.findById((long) customerId);
        if (!customerOpt.isPresent()) {
            throw new IllegalArgumentException("Customer not found");
        }

        CustomersAiAnalysis customer = customerOpt.get();

        // personalizedRecommendations 칼럼에 데이터가 있고 analyzedTime이 30분이 안 지난 시점이라면 DB에서 값 가져오기
        if (customer.getPersonalizedRecommendations() != null &&
                customer.getAnalyzedTime() != null &&
                customer.getAnalyzedTime().isAfter(LocalDateTime.now().minusMinutes(30))) {

            log.info("Returning cached AI analysis for customer: {}", customerId);
            return CustomersAiAnalysisResponse.builder()
                    .name(customer.getName())
                    .frequentOrders(customer.getFrequentOrders())
                    .personalizedRecommendations(customer.getPersonalizedRecommendations())
                    .personalizedRecommendationsReason(customer.getPersonalizedRecommendationsReason())
                    .customerSegments(customer.getCustomerSegments())
                    .analyzedTime(customer.getAnalyzedTime())
                    .build();
        }

        // PurchaseInfo 리스트를 String 리스트로 변환
        List<String> orderDescriptions = customer.getPurchaseInfo().stream()
                .map(info -> info.getOrdNo() + " " + info.getProductName() + " " + info.getOptionValue())
                .collect(Collectors.toList());

        // CustomersAiAnalysisRequest 생성
        CustomersAiAnalysisRequest request = new CustomersAiAnalysisRequest();
        request.setName(customer.getName());
        request.setOrders(orderDescriptions);

        // 파일에서 메시지 읽기
        String aiMessage;
        try {
            aiMessage = new String(Files.readAllBytes(Paths.get("src/main/resources/Script.txt")));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read message from file", e);
        }

        String message = aiMessage + "\n" + request;
        String aiResponse = (openAiChatModel.call(message));

        log.info("AI response received for customer {}: {}", customerId, aiResponse);

        // JSON 문자열을 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode aiResponseNode;

        try {
            // 불필요한 특수문자 제거
            aiResponse = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            aiResponseNode = objectMapper.readTree(aiResponse);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse AI response", e);
        }

        // JSON 데이터를 CustomersAiAnalysisResponse 객체로 변환
        CustomersAiAnalysisResponse response = new CustomersAiAnalysisResponse();
        response.setName(customer.getName());
        response.setFrequentOrders(extractArrayValues(aiResponseNode, "frequentOrders"));
        response.setPersonalizedRecommendations(extractArrayValues(aiResponseNode, "personalizedRecommendations"));
        response.setPersonalizedRecommendationsReason(aiResponseNode.path("personalizedRecommendationsReason").asText());
        response.setCustomerSegments(aiResponseNode.path("customerSegments").asText());
        response.setAnalyzedTime(LocalDateTime.now());

        // CustomersAiAnalysis 업데이트
        customer.setPersonalizedRecommendations(response.getPersonalizedRecommendations());
        customer.setFrequentOrders(response.getFrequentOrders());
        customer.setCustomerSegments(response.getCustomerSegments());
        customer.setPersonalizedRecommendationsReason(response.getPersonalizedRecommendationsReason());
        customer.setAnalyzedTime(response.getAnalyzedTime());

        // 저장
        customersAiAnalysisRepository.save(customer);

        return response;
    }

    @Transactional
    public CustomersAiAnalysisResponse forceUpdateCustomerAnalysis(int customerId) {

        // 데이터베이스에서 고객 정보 조회
        Optional<CustomersAiAnalysis> customerOpt = customersAiAnalysisRepository.findById((long) customerId);
        if (!customerOpt.isPresent()) {
            throw new IllegalArgumentException("Customer not found");
        }

        CustomersAiAnalysis customer = customerOpt.get();
        // PurchaseInfo 리스트를 String 리스트로 변환
        List<String> orderDescriptions = customer.getPurchaseInfo().stream()
                .map(info -> info.getOrdNo() + " " + info.getProductName() + " " + info.getOptionValue())
                .collect(Collectors.toList());

        // CustomersAiAnalysisRequest 생성
        CustomersAiAnalysisRequest request = new CustomersAiAnalysisRequest();
        request.setName(customer.getName());
        request.setOrders(orderDescriptions);

        // 파일에서 메시지 읽기
        String aiMessage;
        try {
            aiMessage = new String(Files.readAllBytes(Paths.get("src/main/resources/Script.txt")));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read message from file", e);
        }

        String message = aiMessage + "\n" + request;
        String aiResponse = (openAiChatModel.call(message));

        log.info("AI response received for customer {}: {}", customerId, aiResponse);

        // JSON 문자열을 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode aiResponseNode;

        try {
            // 불필요한 특수문자 제거
            aiResponse = aiResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            aiResponseNode = objectMapper.readTree(aiResponse);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse AI response", e);
        }

        // JSON 데이터를 CustomersAiAnalysisResponse 객체로 변환
        CustomersAiAnalysisResponse response = new CustomersAiAnalysisResponse();
        response.setName(customer.getName());
        response.setFrequentOrders(extractArrayValues(aiResponseNode, "frequentOrders"));
        response.setPersonalizedRecommendations(extractArrayValues(aiResponseNode, "personalizedRecommendations"));
        response.setPersonalizedRecommendationsReason(aiResponseNode.path("personalizedRecommendationsReason").asText());
        response.setCustomerSegments(aiResponseNode.path("customerSegments").asText());
        response.setAnalyzedTime(LocalDateTime.now());

        // CustomersAiAnalysis 업데이트
        customer.setPersonalizedRecommendations(response.getPersonalizedRecommendations());
        customer.setFrequentOrders(response.getFrequentOrders());
        customer.setCustomerSegments(response.getCustomerSegments());
        customer.setPersonalizedRecommendationsReason(response.getPersonalizedRecommendationsReason());
        customer.setAnalyzedTime(response.getAnalyzedTime());

        // 저장
        customersAiAnalysisRepository.save(customer);

        return response;
    }


    private List<String> extractArrayValues(JsonNode node, String fieldName) {
        List<String> values = new ArrayList<>();
        if (node.has(fieldName) && node.get(fieldName).isArray()) {
            ArrayNode arrayNode = (ArrayNode) node.get(fieldName);
            for (JsonNode jsonNode : arrayNode) {
                values.add(jsonNode.asText());
            }
        }

        return values;
    }
}