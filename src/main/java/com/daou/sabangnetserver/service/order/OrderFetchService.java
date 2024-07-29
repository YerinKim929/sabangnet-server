package com.daou.sabangnetserver.service.order;

import com.daou.sabangnetserver.dto.order.OrderApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
public class OrderFetchService {

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${external.api.order-url}")
    private String orderUrl;

    @Value("${siteCd.no}")
    private String siteCd;

    private final RestTemplate restTemplate = new RestTemplate();

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
}