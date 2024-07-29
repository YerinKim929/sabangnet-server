package com.daou.sabangnetserver.dto.order;

import com.daou.sabangnetserver.dto.table.TableOrdersDetailDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드는 무시
public class OrderApiResponseBase {
    private String ordNo;
    private String ordDttm;
    private String rcvrNm;
    private String rcvrMphnNo;
    private String rcvrBaseAddr;
    private String rcvrDtlsAddr;
    private int sellerNo;
    private List<OrderApiResponseDetail> orderItems;

    // ordPrdNo 리스트를 반환하는 메소드 추가
    public List<Integer> getOrdPrdNos() {
        return orderItems.stream()
                .map(OrderApiResponseDetail::getOrdPrdNo)
                .collect(Collectors.toList());
    }

}