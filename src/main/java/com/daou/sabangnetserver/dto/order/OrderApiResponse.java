package com.daou.sabangnetserver.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderApiResponse {
    @JsonProperty("response")
    private OrderApiResponseListElements response;
}




