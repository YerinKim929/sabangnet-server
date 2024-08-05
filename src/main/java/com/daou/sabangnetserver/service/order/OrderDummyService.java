package com.daou.sabangnetserver.service.order;

import com.daou.sabangnetserver.dto.order.OrderApiResponse;
import com.daou.sabangnetserver.dto.order.OrderApiResponseBase;
import com.daou.sabangnetserver.dto.order.OrderResponseDto;
import com.daou.sabangnetserver.model.OrdersBase;
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
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderDummyService {

    @Autowired
    OrderValidateService orderValidateService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertDummyData(String startDate, String endDate, int sellerNo, List<OrderResponseDto.OrderResult> orderResults) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = classLoader.getResourceAsStream("dummyorder.json")) {
                if (inputStream == null) {
                    throw new IOException("Resource not found: dummyorder.json");
                }
                OrderApiResponse orders = objectMapper.readValue(inputStream, OrderApiResponse.class);

                // Parse startDate and endDate
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate start = LocalDate.parse(startDate, formatter);
                LocalDate end = LocalDate.parse(endDate, formatter);

                // Convert to LocalDateTime for comparison
                LocalDateTime startDateTime = start.atStartOfDay();
                LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

                // DateTimeFormatter for order date format
                DateTimeFormatter orderDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

                // Filter orders based on the date range
                List<OrderApiResponseBase> filteredOrders = orders.getResponse().getListElements().stream()
                        .filter(order -> {
                            LocalDateTime orderDate = LocalDateTime.parse(order.getOrdDttm(), orderDateFormatter);
                            return (orderDate.isEqual(startDateTime) || orderDate.isAfter(startDateTime)) && (orderDate.isEqual(endDateTime) || orderDate.isBefore(endDateTime));
                        })
                        .collect(Collectors.toList());

                orderValidateService.validateOrders(filteredOrders, sellerNo, orderResults);
            }
        } catch (IOException e) {
            log.error("Failed to read dummyorder.json", e);
        }
    }
}