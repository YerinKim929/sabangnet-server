package com.daou.sabangnetserver.dto.table;

import lombok.Data;

import java.util.List;

@Data
public class TableOrdersBaseResponseDto {
    private List<TableOrdersBaseDto> orders;
    private int totalPages;

    public TableOrdersBaseResponseDto(List<TableOrdersBaseDto> orders, int totalPages) {
        this.orders = orders;
        this.totalPages = totalPages;
    }
}