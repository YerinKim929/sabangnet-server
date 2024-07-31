package com.daou.sabangnetserver.service.order;

import com.daou.sabangnetserver.dto.order.OrderApiResponseBase;
import com.daou.sabangnetserver.dto.order.OrderApiResponseDetail;
import com.daou.sabangnetserver.dto.order.OrderResponseDto;
import com.daou.sabangnetserver.model.OrdersDetailId;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import com.daou.sabangnetserver.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OrderValidateService {

    // 토큰 발급을 위한 AuthService DI
    @Autowired
    private AuthService authService;

    @Autowired
    private OrderSaveService orderSaveService;

    // 주문조회 데이터 저장을 위한 ordersBaseRepository DI
    @Autowired
    private OrdersBaseRepository ordersBaseRepository;
    @Autowired
    private OrdersDetailRepository ordersDetailRepository;

    // Datetime 형식 Format
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


    // API에서 불러온 데이터 유효성 검증 확인
    public void validateOrders(List<OrderApiResponseBase> orders, int sellerNo, List<OrderResponseDto.OrderResult> orderResults) {
        // 데이터베이스에서 기존 주문 번호 및 상세 항목을 모두 가져와서 Set(집합)으로 저장
        Set<OrdersDetailId> existingOrderDetailIds = new HashSet<>(ordersDetailRepository.findAll()
                .stream()
                .map(detail -> new OrdersDetailId(detail.getId().getOrdPrdNo(), detail.getId().getOrdNo()))
                .collect(Collectors.toList()));

        // 불러온 주문 목록을 하나씩 검사
        for (OrderApiResponseBase order : orders) {
            // 주문 데이터가 유효한지 검사
            if (isValidOrderData(order)) {
                try {
                    orderSaveService.saveOrders(order, sellerNo, orderResults, existingOrderDetailIds, this);
                } catch (Exception e) {
                    orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), 0, false,"저장 오류 발생"));
                    log.error("Failed to save order: " + order.getOrdNo(), e);
                }
            } else {
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), 0, false, "유효하지 않은 데이터"));
                log.error("Invalid order data: " + order.getOrdNo());
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
    public boolean isValidOrderDetailData(OrderApiResponseDetail detail) {
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


}
