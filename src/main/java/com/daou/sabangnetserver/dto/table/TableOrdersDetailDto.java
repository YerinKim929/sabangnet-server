package com.daou.sabangnetserver.dto.table;

import com.daou.sabangnetserver.model.OrdersDetail;
import lombok.Data;

@Data
public class TableOrdersDetailDto {
    private int index;
    private int ordPrdNo;
    private String prdNm;
    private String optVal;
    private int rowspan;

    public TableOrdersDetailDto(OrdersDetail ordersDetail) {
        this.ordPrdNo = ordersDetail.getId().getOrdPrdNo();
        this.prdNm = ordersDetail.getPrdNm();
        this.optVal = ordersDetail.getOptVal();
    }

    // index와 rowspan을 설정하는 메소드
    public void setIndexAndRowspan(int index, int rowspan) {
        this.index = index;
        this.rowspan = rowspan;
    }
}
