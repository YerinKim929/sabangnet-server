package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.*;
import com.daou.sabangnetserver.dto.order.*;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.model.OrdersDetailId;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

        // 불러온 주문 목록을 테이블 검증하는 함수 (빈데이터 예외 처리)
        if (response.getBody() != null && response.getBody().getResponse() != null) {
            validateOrders(response.getBody().getResponse().getListElements(), sellerNo, orderResults);
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

    // 데이터 유효성 검증 확인
    @Transactional
    private void validateOrders(List<OrderApiResponseBase> orders, int sellerNo, List<OrderResponseDto.OrderResult> orderResults) {
        // 데이터베이스에서 기존 주문 번호 및 상세 항목을 모두 가져와서 Set(집합)으로 저장
        Set<OrdersDetailId> existingOrderDetailIds = new HashSet<>(ordersDetailRepository.findAll()
                .stream()
                .map(detail -> new OrdersDetailId(detail.getId().getOrdPrdNo(), detail.getId().getOrdNo()))
                .collect(Collectors.toList()));

        // 불러온 주문 목록을 하나씩 검사
        for (OrderApiResponseBase order : orders) {
            // 주문 데이터가 유효한지 검사
            if (!isValidOrderData(order)) {
                // 유효하지 않으면 실패 결과 추가 로그에 오류 기록
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), 0, false));
                log.error("Invalid order data: " + order.getOrdNo());
                continue;
            }

            try {
                // 유효한 주문 데이터 저장하는 함수 호출
                saveOrders(order, sellerNo, orderResults, existingOrderDetailIds);
            } catch (Exception e) {
                // 저장에 실패하면 실패 결과를 추가하고 로그에 오류를 기록
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), 0, false));
                log.error("Failed to save order: " + order.getOrdNo(), e);
            }
        }
    }


    // Orderbase 데이터 유효성 검증 함수
    private boolean isValidOrderData(OrderApiResponseBase order) {
        // 각 필드가 null이 아니고 특정 조건을 만족하는지 검사
        if (order.getOrdNo() == null || order.getOrdDttm() == null || order.getRcvrNm() == null ||
                order.getRcvrBaseAddr() == null || order.getRcvrDtlsAddr() == null || order.getRcvrMphnNo() == null) {
            return false; // 하나라도 null이면 유효하지 않음
        }

        // 각 필드의 값이 비어있지 않고 길이 검사
        if (order.getRcvrNm().isEmpty() || order.getRcvrNm().length() > 255 ||
                order.getRcvrBaseAddr().isEmpty() || order.getRcvrBaseAddr().length() > 255 ||
                order.getRcvrDtlsAddr().isEmpty() || order.getRcvrDtlsAddr().length() > 255 ||
                order.getRcvrMphnNo().isEmpty() || order.getRcvrMphnNo().length() > 20) {
            return false; // 조건을 만족하지 않으면 유효하지 않음
        }

        try {
            // 주문 날짜와 시간이 유효한 형식 검사
            LocalDateTime.parse(order.getOrdDttm(), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return false; // 형식이 맞지 않으면 유효하지 않음
        }

        // 수신자 전화번호가 10자리 또는 11자리 숫자인지 검사
        if (!order.getRcvrMphnNo().matches("\\d{10,11}")) {
            return false; // 조건을 만족하지 않으면 유효하지 않음
        }

        return true; // 모든 조건을 만족하면 유효
    }


    // OrderDetail 데이터 유효성 검증 함수
    private boolean isValidOrderDetailData(OrderApiResponseDetail detail) {
        // 각 필드가 적절한 값을 가지고 있는지 검사
        if (detail.getOrdPrdNo() == 0 || detail.getOrdNo() == null || detail.getPrdNm() == null || detail.getOptVal() == null) {
            return false; // 하나라도 조건을 만족하지 않으면 유효하지 않음
        }

        // 각 필드의 값이 비어있지 않고 길이가 적절한지 검사
        if (detail.getPrdNm().isEmpty() || detail.getPrdNm().length() > 255 ||
                detail.getOptVal().isEmpty() || detail.getOptVal().length() > 255) {
            return false; // 조건을 만족하지 않으면 유효하지 않음
        }

        return true; // 모든 조건을 만족하면 유효
    }

    // 주문 데이터 저장 함수
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrders(OrderApiResponseBase order, int sellerNo, List<OrderResponseDto.OrderResult> orderResults, Set<OrdersDetailId> existingOrderDetailIds) {
        // 데이터베이스에서 주어진 ordNo에 해당하는 OrdersBase 객체 반환 (결과 API에서 바로 OrdNo 가져와서 찾음)
        OrdersBase ordersBase = ordersBaseRepository.findById(order.getOrdNo()).orElse(new OrdersBase());

        // 주문 기본 정보 객체에 데이터를 설정
        ordersBase.setOrdNo(order.getOrdNo());
        ordersBase.setOrdDttm(LocalDateTime.parse(order.getOrdDttm(), DATE_TIME_FORMATTER));
        ordersBase.setRcvrNm(order.getRcvrNm());
        ordersBase.setRcvrAddr(order.getRcvrBaseAddr() + " " + order.getRcvrDtlsAddr());
        ordersBase.setRcvrMphnNo(order.getRcvrMphnNo());
        ordersBase.setSellerNo(sellerNo);
        ordersBase.setOrdCollectDttm(LocalDateTime.parse(LocalDateTime.now().format(DATE_TIME_FORMATTER), DATE_TIME_FORMATTER));

        // 주문 기본 정보 저장
        saveOrderBase(ordersBase);

        // 저장할 세부목록 있는지
        boolean hasSavedDetails = false;
        // 저장된 세부목록 있는지
        boolean hasExistingDetails = false;

        // 주문 상세 항목들 처리
        for (OrderApiResponseDetail item : order.getOrderItems()) {
            // 주문 상세 항목의 고유 ID를 생성
            OrdersDetailId detailId = new OrdersDetailId(item.getOrdPrdNo(), item.getOrdNo());


            // 중복 여부 검사
            if (existingOrderDetailIds.contains(detailId)) {
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), false));
                log.error("Duplicate order detail data: " + item.getOrdPrdNo() + " for order: " + order.getOrdNo());
                hasExistingDetails = true;
                continue;
            }

            try {
                // 주문 상세 데이터가 유효한지 검사
                if (!isValidOrderDetailData(item)) {
                    orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), false));
                    log.error("Invalid order detail data: " + item.getOrdPrdNo() + " for order: " + order.getOrdNo());
                    continue;
                }

                // 주문 상세 정보를 설정하고 추가
                OrdersDetail ordersDetail = new OrdersDetail();
                ordersDetail.setId(detailId);
                ordersDetail.setOrdersBase(ordersBase);
                ordersDetail.setPrdNm(item.getPrdNm());
                ordersDetail.setOptVal(item.getOptVal());

                ordersBase.addOrderDetail(ordersDetail);
                saveOrderDetail(ordersDetail);
                existingOrderDetailIds.add(detailId);
                hasSavedDetails = true;

                // 성공 결과 추가
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), true));

            } catch (Exception e) {
                // 저장에 실패하면 실패 결과를 추가하고 로그에 오류를 기록
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), false));
                log.error("Failed to save order detail: " + item.getOrdPrdNo() + " for order: " + order.getOrdNo(), e);
            }
            // 세부 목록이 하나도 저장되지 않았고 기존 세부 목록도 없다면 OrdersBase 삭제
            if (!hasSavedDetails && !hasExistingDetails) {
                ordersBaseRepository.delete(ordersBase);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrderBase(OrdersBase ordersBase) {
        ordersBaseRepository.save(ordersBase);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrderDetail(OrdersDetail ordersDetail) {
        ordersDetailRepository.save(ordersDetail);
    }


    @Transactional
    public void insertDummyData(String startDate, String endDate, List<OrderResponseDto.OrderResult> orderResults) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            // dummyorder.json 파일에서 OrdersBase 리스트를 읽어오기
            List<OrdersBase> orders = objectMapper.readValue(new File("src/main/resources/dummyorder.json"), new TypeReference<List<OrdersBase>>() {
            });
            List<OrdersBase> filteredOrders = new ArrayList<>();

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

            // 기존에 데이터베이스에 있는 주문 번호와 세부 주문 번호를 조회하여 Set으로 저장
            Set<OrdersDetailId> existingOrderDetailIds = new HashSet<>(ordersDetailRepository.findAll()
                    .stream()
                    .map(detail -> new OrdersDetailId(detail.getId().getOrdPrdNo(), detail.getId().getOrdNo()))
                    .collect(Collectors.toList()));

            // 필터링된 각 주문을 처리
            for (OrdersBase order : filteredOrders) {
                // ordersBaseRepository에 현재 주문 번호가 존재하는지 확인
                if (!ordersBaseRepository.existsById(order.getOrdNo())) {
                    // 주문 기본 정보 검증
                    if (!isValidOrderDummyData(order)) {
                        orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo().toString(), 0, false));
                        log.error("Invalid order data: " + order.getOrdNo());
                        continue;
                    }
                    // 주문 번호가 존재하지 않는다면 saveOrdersDummy 메서드를 호출하여 주문을 저장
                    saveOrdersDummy(order, orderResults, existingOrderDetailIds);


                } else {
                    // 주문 번호가 존재한다면 중복된 주문 세부 목록을 기록
                    for (OrdersDetail detail : order.getOrdersDetail()) {
                        // 각 세부 목록의 ID를 생성
                        OrdersDetailId detailId = new OrdersDetailId(detail.getId().getOrdPrdNo(), detail.getId().getOrdNo());
                        // existingOrderDetailIds Set에 현재 세부 목록의 ID가 포함되어 있는지 확인
                        if (existingOrderDetailIds.contains(detailId)) {
                            // 중복된 세부 목록이라면 orderResults에 실패 결과를 추가하고, 로그를 남김
                            orderResults.add(new OrderResponseDto.OrderResult(detail.getId().getOrdNo().toString(), detail.getId().getOrdPrdNo(), false));
                            log.error("Duplicate order detail data: " + detail.getId().getOrdPrdNo() + " for order: " + order.getOrdNo());
                        }
                    }
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrdersDummy(OrdersBase order, List<OrderResponseDto.OrderResult> orderResults, Set<OrdersDetailId> existingOrderDetailIds) {

        // 먼저 주문 기본 정보를 데이터베이스에 먼저 저장
        // 기본 정보가 저장된 후에야 세부 정보가 이 기본 정보를 참조해 DB에 저장할 수 있음
        saveOrderBase(order);

        // 저장할 세부 주문 목록을 담을 리스트 생성
        List<OrdersDetail> detailsToAdd = new ArrayList<>();
        boolean hasSavedDetails = false; // 세부 주문이 저장되었는지 여부를 추적하는 플래그

        // 각 세부 주문을 반복 처리
        for (OrdersDetail detail : order.getOrdersDetail()) {
            OrdersDetailId detailId = new OrdersDetailId(detail.getId().getOrdPrdNo(), detail.getId().getOrdNo());

            // 세부 주문이 이미 존재하는지 확인
            if (existingOrderDetailIds.contains(detailId)) {
                // 이미 존재하면 실패로 결과 추가
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), detail.getId().getOrdPrdNo(), false));
                log.error("Duplicate order detail data: " + detail.getId().getOrdPrdNo() + " for order: " + order.getOrdNo());
                continue;
            }

            try {
                // 세부 주문 데이터가 유효한지 확인
                if (!isValidOrderDetailData(detail)) {
                    // 유효하지 않으면 실패로 결과 추가
                    orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), detail.getId().getOrdPrdNo(), false));
                    log.error("Invalid order detail data: " + detail.getId().getOrdPrdNo() + " for order: " + order.getOrdNo());
                    continue;
                }

                // 세부 주문을 기본 주문과 연결
                detail.setOrdersBase(order);

                //저장할 세부 주문 목록에 현재 세부 주문을 추가
                detailsToAdd.add(detail);

                // 중복 확인을 위해 저장된 세부 주문 ID 추가
                existingOrderDetailIds.add(detailId);

                // ResponseDto에 성공 결과 데이터 추가
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), detail.getId().getOrdPrdNo(), true));

                hasSavedDetails = true; // 세부 주문이 저장되었음을 표시

            } catch (Exception e) {
                // 세부 주문 저장 실패 시 결과 추가
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), detail.getId().getOrdPrdNo(), false));
                log.error("Failed to save order detail: " + detail.getId().getOrdPrdNo() + " for order: " + order.getOrdNo(), e);
            }
        }

        // 주문 기본 정보에 저장할 세부 주문 목록 추가
        order.getOrdersDetail().addAll(detailsToAdd);

        // 세부 주문이 하나도 저장되지 않았고, 기존에 세부 주문이 전혀 없을 때 기본 주문 삭제
        if (!hasSavedDetails && order.getOrdersDetail().isEmpty()) {
            ordersBaseRepository.delete(order);
            log.info("Deleted OrdersBase due to no valid order details: " + order.getOrdNo());
        }
    }

    // 더미 데이터용 Orderbase 검증 함수
    private boolean isValidOrderDummyData(OrdersBase order) {
        if (order.getOrdNo() == null || order.getOrdDttm() == null || order.getRcvrNm() == null ||
                order.getRcvrAddr() == null || order.getRcvrMphnNo() == null) {
            return false;
        }

        if (order.getRcvrNm().isEmpty() || order.getRcvrNm().length() > 255 ||
                order.getRcvrAddr().isEmpty() || order.getRcvrAddr().length() > 255 ||
                order.getRcvrMphnNo().isEmpty() || order.getRcvrMphnNo().length() > 20) {
            return false;
        }

        try {
            order.getOrdDttm();
        } catch (Exception e) {
            return false;
        }

        if (!order.getRcvrMphnNo().matches("\\d{10,11}")) {
            return false;
        }

        return true;
    }

    // 더미 데이터용 Orderdetail 검증 함수
    private boolean isValidOrderDetailData(OrdersDetail detail) {
        if (detail.getId().getOrdPrdNo() == 0 || detail.getId().getOrdNo() == null || detail.getPrdNm() == null || detail.getOptVal() == null) {
            return false;
        }

        if (detail.getPrdNm().isEmpty() || detail.getPrdNm().length() > 255 ||
                detail.getOptVal().isEmpty() || detail.getOptVal().length() > 255) {
            return false;
        }

        return true;
    }

}
