package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.CustomersAiAnalysisTableResponse;
import com.daou.sabangnetserver.model.CustomersAiAnalysis;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.repository.CustomersAiAnalysisRepository;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomersAiAnalysisService {

    @Autowired
    private CustomersAiAnalysisRepository customersAiAnalysisRepository;

    @Autowired
    private OrdersBaseRepository ordersBaseRepository;

    public Map<String, List<CustomersAiAnalysisTableResponse>> updateAllCustomerAnalysisTable() {
        List<OrdersBase> ordersBases = ordersBaseRepository.findAll();

        for (OrdersBase ordersBase : ordersBases) {
            String name = ordersBase.getRcvrNm();
            String phoneNumber = ordersBase.getRcvrMphnNo();

            Optional<CustomersAiAnalysis> existingAnalysis = customersAiAnalysisRepository.findByNameAndPhoneNumber(name, phoneNumber);
            if (existingAnalysis.isEmpty()) {
                List<OrdersDetail> ordersDetails = ordersBase.getOrdersDetail();
                List<CustomersAiAnalysis.PurchaseInfo> purchaseInfoList = ordersDetails.stream()
                        .map(detail -> CustomersAiAnalysis.PurchaseInfo.builder()
                                .productName(detail.getPrdNm())
                                .optionValue(detail.getOptVal())
                                .build())
                        .collect(Collectors.toList());

                CustomersAiAnalysis analysis = CustomersAiAnalysis.builder()
                        .name(name)
                        .phoneNumber(phoneNumber)
                        .aiCollected(false) // 기본값으로 false 설정, 필요한 경우 변경 가능
                        .purchaseInfo(purchaseInfoList)
                        .build();

                customersAiAnalysisRepository.save(analysis);
            }
        }

        List<CustomersAiAnalysis> analyses = customersAiAnalysisRepository.findAll();
        List<CustomersAiAnalysisTableResponse> responseList = analyses.stream()
                .map(analysis -> new CustomersAiAnalysisTableResponse(
                        analysis.getId(),
                        analysis.getName(),
                        analysis.getPhoneNumber(),
                        analysis.isAiCollected()
                ))
                .collect(Collectors.toList());


        Map<String, List<CustomersAiAnalysisTableResponse>> responseMap = new HashMap<>();
        responseMap.put("orders", responseList);
        return responseMap;
    }

}
