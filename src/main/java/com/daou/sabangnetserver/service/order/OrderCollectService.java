package com.daou.sabangnetserver.service.order;

import com.daou.sabangnetserver.dto.order.*;
import com.daou.sabangnetserver.dto.token.TokenRequestDto;
import com.daou.sabangnetserver.dto.token.TokenResponseDto;
import com.daou.sabangnetserver.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderCollectService {

    // 토큰 발급을 위한 AuthService DI
    @Autowired
    private AuthService authService;

    @Autowired
    private OrderValidateService orderValidateService;

    @Autowired
    private OrderFetchService orderFetchService;

    @Autowired
    private OrderDummyService orderDummyService;


    @Value("${apiKey.no}")
    private String apiKey; // apikey

    @Value("${sltnCd.no}")
    private String sltnCd; // 인증값


    // 주문 데이터를 타다닥 API로부터 가져와서 데이터베이스에 저장하는 함수
    public OrderResponseDto fetchAndSaveOrders(OrderRequestDto orderRequestDto) {

        int sellerNo = orderRequestDto.getSellerNo();
        String startDate = orderRequestDto.getStartDate();
        String endDate = orderRequestDto.getEndDate();
        String status = orderRequestDto.getStatus();

        // 토큰 값 받기
        TokenRequestDto tokenRequest = new TokenRequestDto(apiKey, sltnCd);
        TokenResponseDto tokenResponse = authService.validateAndRefreshTokens(tokenRequest, sellerNo);
        String accessToken = tokenResponse.getAccessToken();

        // 주문 목록 조회하는 함수
        ResponseEntity<OrderApiResponse> response = orderFetchService.fetchOrders(sellerNo, startDate, endDate, status, accessToken);

        log.info(String.valueOf(response.getBody()));

        // 주문 결과를 저장할 리스트 생성
        List<OrderResponseDto.OrderResult> orderResults = new ArrayList<>();

        // 불러온 주문 목록을 검증 하는 함수 (빈데이터 예외 처리)
        // 1. 데이터 검증 / 2. 데이터 중복 검사
        // 2-1. 이미 있는 주문번호여도 세부 주문 번호 없으면 db 삽입
        if (response.getBody() != null && response.getBody().getResponse() != null) {
            orderValidateService.validateOrders(response.getBody().getResponse().getListElements(), sellerNo, orderResults);
        }

        log.info("더미 데이터 삽입");
        // 1. 데이터 가공
        // 2. 기존 데이터 중복 검사
        // 2.1 주문번호가 중복일 때 & 2.2 주문번호와 세부번호가 중복일 때 고려
        // 3. 데이터 중복 검사
        orderDummyService.insertDummyData(startDate, endDate, sellerNo, orderResults);

        // 성공 및 실패 카운트 계산
        int successCount = (int) orderResults.stream().filter(OrderResponseDto.OrderResult::isSuccess).count();
        int failCount = orderResults.size() - successCount;

        // OrderResponseDto 생성 및 반환
        return new OrderResponseDto(orderResults, orderResults.size(), successCount, failCount);
    }

}
