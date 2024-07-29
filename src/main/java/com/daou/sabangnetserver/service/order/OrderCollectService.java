package com.daou.sabangnetserver.service.order;

import com.daou.sabangnetserver.dto.*;
import com.daou.sabangnetserver.dto.order.*;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.model.OrdersDetailId;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import com.daou.sabangnetserver.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    // 주문조회 데이터 저장을 위한 ordersBaseRepository DI
    @Autowired
    private OrdersBaseRepository ordersBaseRepository;
    @Autowired
    private OrdersDetailRepository ordersDetailRepository;

    // 환경 변수 값 불러오기
    @Value("${external.api.base-url}")
    private String baseUrl; // 기본 url

    @Value("${external.api.order-url}")
    private String orderUrl; // 주소 url

    @Value("${apiKey.no}")
    private String apiKey; // apikey

    @Value("${sltnCd.no}")
    private String sltnCd; // 인증값

    @Value("${siteCd.no}")
    private String siteCd; // 사이트 코드

    // Datetime 형식 Format
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // RestTemplate 인스턴스 생성
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
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
        orderDummyService.insertDummyData(startDate, endDate, orderResults);

        // 성공 및 실패 카운트 계산
        int successCount = (int) orderResults.stream().filter(OrderResponseDto.OrderResult::isSuccess).count();
        int failCount = orderResults.size() - successCount;

        // OrderResponseDto 생성 및 반환
        return new OrderResponseDto(orderResults, orderResults.size(), successCount, failCount);
    }

}
