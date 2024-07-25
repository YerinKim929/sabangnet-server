package com.daou.sabangnetserver.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드는 무시
public class OrderApiResponseDetail {
    private int ordPrdNo;
    private String ordNo;
    private String prdNm;
    private String optVal;
}