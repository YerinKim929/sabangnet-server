package com.daou.sabangnetserver.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

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
}