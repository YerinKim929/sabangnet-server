package com.daou.sabangnetserver.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderApiResponse {
    private OrderApiResponseListElements response;
}




