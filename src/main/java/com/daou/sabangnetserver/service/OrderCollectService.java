package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.*;
import com.daou.sabangnetserver.dto.order.*;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.model.OrdersDetailId;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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


    // @PersistenceContext 어노테이션은 Spring에서 JPA의 EntityManager를 주입받기 위해 사용 (영속성 컨텍스트)
    // EntityManager는 기존 Repository 인터페이스 보다 데이터베이스 작업을 보다 세밀하게 제어가능
    @PersistenceContext
    private EntityManager entityManager;

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
        ResponseEntity<OrderApiResponse> response = fetchOrders(sellerNo, startDate, endDate, status, accessToken);

        // 주문 결과를 저장할 리스트 생성
        List<OrderResponseDto.OrderResult> orderResults = new ArrayList<>();

        // 불러온 주문 목록을 테이블 저장하는 함수 (빈데이터 예외 처리)
        if (response.getBody() != null && response.getBody().getResponse() != null) {
            saveOrders(response.getBody().getResponse().getListElements(), sellerNo, orderResults);
        }

        log.info("더미 데이터 삽입");
        insertDummyData(startDate, endDate, orderResults);

        // 성공 및 실패 카운트 계산
        int successCount = (int) orderResults.stream().filter(OrderResponseDto.OrderResult::isSuccess).count();
        int failCount = orderResults.size() - successCount;

        // OrderResponseDto 생성 및 반환
        return new OrderResponseDto(orderResults, orderResults.size(), successCount, failCount);
    }

    // 타다닥 API에서 주문 목록 결과 받아오는 함수
    public ResponseEntity<OrderApiResponse> fetchOrders(int sellerNo, String startDate, String endDate, String status, String accessToken) {

        // 파라미터 삽입하여 URL 작성
        String sumUrl = baseUrl + orderUrl;
        String sellerOrderUrl = sumUrl.replace("sellerNo", String.valueOf(sellerNo));

        String newStartDate = startDate.replace("-", "/");
        String newEndDate = endDate.replace("-", "/");

        URI uri = UriComponentsBuilder.fromHttpUrl(sellerOrderUrl)
                .queryParam("ordDtFrom", newStartDate)
                .queryParam("ordDtTo", newEndDate)
                .queryParam("siteCd", siteCd)
                .queryParam("status", status)
                .build()
                .toUri();
        log.info("요청 URI : " + uri);

        // Http Request
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        // Http Response
        ResponseEntity<String> response = restTemplate.exchange(
                uri, HttpMethod.GET, entity, String.class); // 응답 String으로 받음


        // JSON 객체를 DTO로 변환
        ObjectMapper mapper = new ObjectMapper();
        OrderApiResponse orderApiResponse = null;
        try {
            orderApiResponse = mapper.readValue(response.getBody(), OrderApiResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // 타다닥 API 주문조회 Response 반환
        return new ResponseEntity<>(orderApiResponse, response.getStatusCode());
    }

    @Transactional
    private void saveOrders(List<OrderApiResponseBase> orders, int sellerNo, List<OrderResponseDto.OrderResult> orderResults) {

        // 주문 번호 목록을 추출하여 리스트로 저장
        List<String> orderNos = orders.stream()
                .map(OrderApiResponseBase::getOrdNo)
                .collect(Collectors.toList());

        // 기존에 데이터베이스에 있는 주문 번호를 조회하여 Set으로 저장
        Set<String> existingOrderNos = new HashSet<>(ordersBaseRepository.findAllById(orderNos)
                .stream()
                .map(OrdersBase::getOrdNo)
                .collect(Collectors.toList()));


        int batchSize = 200; // 한 번에 처리할 배치 크기
        int count = 0; // 현재 처리된 주문의 개수

        // OrderApiResponseBase에 속하는 주문 하나하나 마다 반복
        for (OrderApiResponseBase order : orders) {
            // 이미 데이터베이스에 존재하는 주문 번호는 넘기기
            if (existingOrderNos.contains(order.getOrdNo())) {
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), false));
                continue;
            }

            // ordersBase 테이블에 넣을 객체 생성
            try {
                // ordersBase 테이블에 넣을 객체 생성
                OrdersBase ordersBase = new OrdersBase();
                ordersBase.setOrdNo(order.getOrdNo());
                ordersBase.setOrdDttm(LocalDateTime.parse(order.getOrdDttm(), DATE_TIME_FORMATTER));
                ordersBase.setRcvrNm(order.getRcvrNm());
                ordersBase.setRcvrAddr(order.getRcvrBaseAddr() + " " + order.getRcvrDtlsAddr());
                ordersBase.setRcvrMphnNo(order.getRcvrMphnNo());
                ordersBase.setSellerNo(sellerNo);
                ordersBase.setOrdCollectDttm(LocalDateTime.parse(LocalDateTime.now().format(DATE_TIME_FORMATTER), DATE_TIME_FORMATTER));

                // 생성한 OrdersBase 객체를 데이터베이스에 저장
                entityManager.persist(ordersBase);
                count++; // 처리된 주문 개수를 증가

                // 주문 하나에 속하는 세부주문(OrderApiResponseDetail) 마다 반복
                for (OrderApiResponseDetail item : order.getOrderItems()) {
                    OrdersDetailId detailId = new OrdersDetailId(item.getOrdPrdNo(), item.getOrdNo()); // 복합키 선언

                    // ordersDetail 테이블에 넣을 객체 생성
                    OrdersDetail ordersDetail = new OrdersDetail();
                    ordersDetail.setId(detailId);
                    ordersDetail.setOrdersBase(ordersBase);
                    ordersDetail.setPrdNm(item.getPrdNm());
                    ordersDetail.setOptVal(item.getOptVal());

                    entityManager.persist(ordersDetail);
                    count++;
                }

                // 배치 처리 및 클리어
                if (count % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }

                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), true));
            } catch (Exception e) {
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), false));
                log.error("Failed to save order: " + order.getOrdNo(), e);
            }
        }

        // 남아 있는 데이터 플러시 및 클리어
        if (count % batchSize != 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }


    @Transactional
    public void insertDummyData(String startDate, String endDate, List<OrderResponseDto.OrderResult> orderResults) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            List<OrdersBase> orders = objectMapper.readValue(new File("src/main/resources/dummyorder.json"), new TypeReference<List<OrdersBase>>() {});
            List<OrdersBase> filteredOrders = new ArrayList<>();
            List<OrdersDetail> allDetails = new ArrayList<>();

            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime now = LocalDateTime.now();
            String nowFormatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            for (OrdersBase order : orders) {
                LocalDate orderDate = order.getOrdDttm().toLocalDate();
                if ((orderDate.isEqual(start) || orderDate.isAfter(start)) && (orderDate.isEqual(end) || orderDate.isBefore(end))) {
                    order.setOrdCollectDttm(LocalDateTime.parse(nowFormatted));
                    filteredOrders.add(order);
                }
            }

            for (OrdersBase order : filteredOrders) {
                if (!ordersBaseRepository.existsById(order.getOrdNo())) {
                    try {
                        ordersBaseRepository.save(order);
                        allDetails.addAll(order.getOrdersDetail());
                        orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), true));
                    } catch (Exception e) {
                        orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), false));
                        log.error("Failed to save order: " + order.getOrdNo(), e);
                    }
                } else {
                    orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), false));
                }
            }

            ordersDetailRepository.saveAll(allDetails);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
