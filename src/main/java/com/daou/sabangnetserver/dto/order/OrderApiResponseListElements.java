package com.daou.sabangnetserver.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderApiResponseListElements {
    private List<OrderApiResponseBase> listElements;
}