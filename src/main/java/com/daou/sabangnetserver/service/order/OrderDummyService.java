package com.daou.sabangnetserver.service.order;

import com.daou.sabangnetserver.dto.order.OrderResponseDto;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.model.OrdersDetailId;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrderDummyService {

    // 주문조회 데이터 저장을 위한 ordersBaseRepository DI
    @Autowired
    private OrdersBaseRepository ordersBaseRepository;
    @Autowired
    private OrdersDetailRepository ordersDetailRepository;

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
                    // 주문 번호가 존재한다면 중복되지 않은 세부 주문을 저장
                    OrdersBase existingOrderBase = ordersBaseRepository.findById(order.getOrdNo()).orElse(null);
                    // 현재 주문 번호가 DB에 있는지 확인하고, 존재한다면 해당 OrdersBase 엔티티를 가져옴

                    if (existingOrderBase != null) {
                        for (OrdersDetail detail : order.getOrdersDetail()) { // OrdersBase에 포함된 각 OrdersDetail을 순회
                            OrdersDetailId detailId = new OrdersDetailId(detail.getId().getOrdPrdNo(), detail.getId().getOrdNo());

                            // 주문 번호가 존재하는데, 처음 불러오는 주문 세부 목록을 기록 (detailId가 중복되지 않은 경우)
                            if (!existingOrderDetailIds.contains(detailId)) {

                                if (isValidOrderDetailData(detail)) {
                                    // OrderDetail 데이터가 유효한지 확인
                                    detail.setOrdersBase(existingOrderBase);
                                    // OrdersDetail 엔티티에 OrdersBase 엔티티를 설정
                                    saveOrderDetail(detail);
                                    // OrdersDetail 엔티티를 DB에 저장
                                    existingOrderDetailIds.add(detailId);
                                    // 새로운 OrderDetail ID를 기존 ID 목록에 추가
                                    orderResults.add(new OrderResponseDto.OrderResult(detail.getId().getOrdNo(), detail.getId().getOrdPrdNo(), true));
                                    // 성공 저장 결과를 OrderResults 목록에 추가

                                } else {
                                    // OrderDetail 데이터가 유효하지 않은 경우
                                    orderResults.add(new OrderResponseDto.OrderResult(detail.getId().getOrdNo(), detail.getId().getOrdPrdNo(), false));
                                    // 유효하지 않은 데이터를 OrderResults 목록에 실패로 추가
                                    log.error("Invalid order detail data: " + detail.getId().getOrdPrdNo() + " for order: " + order.getOrdNo());
                                    // 유효하지 않은 데이터에 대한 에러 로그를 기록
                                }
                            } else {
                                // 기존 OrderDetail ID 목록에 포함된 경우(즉, 중복된 경우)
                                orderResults.add(new OrderResponseDto.OrderResult(detail.getId().getOrdNo(), detail.getId().getOrdPrdNo(), false));
                                // 중복된 데이터를 OrderResults 목록에 실패로 추가
                                log.error("Duplicate order detail data: " + detail.getId().getOrdPrdNo() + " for order: " + order.getOrdNo());
                                // 중복된 데이터에 대한 에러 로그를 기록
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrderBase(OrdersBase ordersBase) {
        ordersBaseRepository.save(ordersBase);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrderDetail(OrdersDetail ordersDetail) {
        ordersDetailRepository.save(ordersDetail);
    }

}
