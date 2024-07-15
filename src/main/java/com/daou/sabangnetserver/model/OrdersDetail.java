package com.daou.sabangnetserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrdersDetail {

    @EmbeddedId
    private OrdersDetailId id;

    // 주문 번호 (복합키의 일부, 외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ordNo")
    @JoinColumn(name = "ordNo", nullable = false)
    private OrdersBase ordersBase;

    // 상품명
    @Column(nullable = false)
    private String prdNm;

    // 주문 상품 옵션 값
    private String optVal;

}
