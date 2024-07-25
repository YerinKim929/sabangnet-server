package com.daou.sabangnetserver.dto.table;

import com.daou.sabangnetserver.model.OrdersBase;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TableOrdersBaseDto {
    private String ordNo;
    private LocalDateTime ordDttm;
    private String rcvrNm;
    private String rcvrAddr;
    private String rcvrMphnNo;
    private Integer sellerNo;
    private LocalDateTime ordCollectDttm;

    @Setter
    private List<TableOrdersDetailDto> ordersDetail;

    public TableOrdersBaseDto(OrdersBase ordersBase) {
        this.ordNo = ordersBase.getOrdNo();
        this.ordDttm = ordersBase.getOrdDttm();
        this.rcvrNm = ordersBase.getRcvrNm();
        this.rcvrAddr = ordersBase.getRcvrAddr();
        this.rcvrMphnNo = ordersBase.getRcvrMphnNo();
        this.sellerNo = ordersBase.getSellerNo();
        this.ordCollectDttm = ordersBase.getOrdCollectDttm();
    }

}
