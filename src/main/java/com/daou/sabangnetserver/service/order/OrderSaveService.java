package com.daou.sabangnetserver.service.order;

import com.daou.sabangnetserver.dto.order.OrderApiResponseBase;
import com.daou.sabangnetserver.dto.order.OrderApiResponseDetail;
import com.daou.sabangnetserver.dto.order.OrderResponseDto;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.model.OrdersDetailId;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
public class OrderSaveService {

    // 주문조회 데이터 저장을 위한 ordersBaseRepository DI
    @Autowired
    private OrdersBaseRepository ordersBaseRepository;
    @Autowired
    private OrdersDetailRepository ordersDetailRepository;

    // Datetime 형식 Format
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // 주문 데이터 저장 함수
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrders(OrderApiResponseBase order,
                           int sellerNo,
                           List<OrderResponseDto.OrderResult> orderResults,
                           Set<OrdersDetailId> existingOrderDetailIds,
                           OrderValidateService orderValidateService) {

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

        // 주문 상세 항목들 처리
        for (OrderApiResponseDetail item : order.getOrderItems()) {
            // 주문 상세 항목의 고유 ID를 생성
            OrdersDetailId detailId = new OrdersDetailId(item.getOrdPrdNo(), item.getOrdNo());

            // 중복 여부 검사
            if (existingOrderDetailIds.contains(detailId)) {
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), false,"이미 수집된 주문 데이터입니다."));
                log.error("Duplicate order detail data: " + item.getOrdPrdNo() + " for order: " + order.getOrdNo());
                continue;
            }

            try {
                // 주문 상세 데이터가 유효한지 검사
                if (!orderValidateService.isValidOrderDetailData(item)) {
                    orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), false, "[상세주문] 유효하지 않은 데이터입니다."));
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

                // 성공 결과 추가
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), true,""));

            } catch (Exception e) {
                // 저장에 실패하면 실패 결과를 추가하고 로그에 오류를 기록
                orderResults.add(new OrderResponseDto.OrderResult(order.getOrdNo(), item.getOrdPrdNo(), false,"저장하는 도중 오류가 발생했습니다."));
                log.error("Failed to save order detail: " + item.getOrdPrdNo() + " for order: " + order.getOrdNo(), e);
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
}
