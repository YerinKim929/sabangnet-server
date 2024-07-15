package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.*;
import com.daou.sabangnetserver.dto.order.OrderApiResponse;
import com.daou.sabangnetserver.dto.order.OrderApiResponseBase;
import com.daou.sabangnetserver.dto.order.OrderApiResponseDetail;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.model.OrdersDetailId;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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


import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class OrderCollectService {

    @Autowired
    private AuthService authService;

    @Autowired
    private OrdersBaseRepository ordersBaseRepository;
    @Autowired
    private OrdersDetailRepository ordersDetailRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${external.api.order-url}")
    private String orderUrl;

    @Value("${apiKey.no}")
    private String apiKey;

    @Value("${sltnCd.no}")
    private String sltnCd;

    @Value("${siteCd.no}")
    private String siteCd;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // 주문 데이터를 외부 API로부터 가져와서 데이터베이스에 저장하는 함수
    public void fetchAndSaveOrders(int sellerNo, String startDate, String endDate, String status) {

        TokenRequestDto tokenRequest = new TokenRequestDto(apiKey, sltnCd);
        TokenResponseDto tokenResponse = authService.validateAndRefreshTokens(tokenRequest, sellerNo);
        String accessToken = tokenResponse.getAccessToken();

        ResponseEntity<OrderApiResponse> response = fetchOrders(startDate, endDate, status, accessToken);

        if (response.getBody() != null && response.getBody().getResponse() != null) {
            saveOrders(response.getBody().getResponse().getListElements());
        }
    }

    // 타다닥 API에서 결과 받아오는 함수
    public ResponseEntity<OrderApiResponse> fetchOrders(String startDate, String endDate, String status, String accessToken) {
        String sumUrl = baseUrl + orderUrl;

        URI uri = UriComponentsBuilder.fromHttpUrl(sumUrl)
                .queryParam("ordDtFrom", startDate)
                .queryParam("ordDtTo", endDate)
                .queryParam("siteCd", siteCd)
                .queryParam("status", status)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        log.info("요청 URI : " + uri);

        ResponseEntity<String> response = restTemplate.exchange(
                uri, HttpMethod.GET, entity, String.class); // 응답을 String으로 받아 디버깅


        // String 응답을 DTO로 변환
        ObjectMapper mapper = new ObjectMapper();
        OrderApiResponse orderApiResponse = null;
        try {
            orderApiResponse = mapper.readValue(response.getBody(), OrderApiResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(orderApiResponse, response.getStatusCode());
    }


    // 테이블에 저장하는 함수
    private void saveOrders(List<OrderApiResponseBase> orders) {

        for (OrderApiResponseBase order : orders) {
            if (ordersBaseRepository.existsById(order.getOrdNo())) {
                continue;
            }

            OrdersBase ordersBase = new OrdersBase();
            ordersBase.setOrdNo(order.getOrdNo());
            ordersBase.setOrdDttm(LocalDateTime.parse(order.getOrdDttm(), DATE_TIME_FORMATTER));
            ordersBase.setRcvrNm(order.getRcvrNm());
            ordersBase.setRcvrAddr(order.getRcvrBaseAddr() + " " + order.getRcvrDtlsAddr());
            ordersBase.setRcvrMphnNo(order.getRcvrMphnNo());
            ordersBase.setSellerNo(order.getSellerNo());
            ordersBase.setOrdCollectDttm(LocalDateTime.parse(LocalDateTime.now().format(DATE_TIME_FORMATTER),DATE_TIME_FORMATTER));

            ordersBaseRepository.save(ordersBase);

            for (OrderApiResponseDetail item : order.getOrderItems()) {
                OrdersDetailId detailId = new OrdersDetailId(item.getOrdPrdNo(), item.getOrdNo());
                OrdersDetail ordersDetail = new OrdersDetail();
                ordersDetail.setId(detailId);
                ordersDetail.setOrdersBase(ordersBase);
                ordersDetail.setPrdNm(item.getPrdNm());
                ordersDetail.setOptVal(item.getOptVal());

                ordersDetailRepository.save(ordersDetail);
            }
        }
    }
}

