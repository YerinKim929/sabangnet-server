package com.daou.sabangnetserver.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders_detail")
@BatchSize(size = 200)
public class OrdersDetail {

    @EmbeddedId
    private OrdersDetailId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ordNo")
    @JoinColumn(name = "ord_no", nullable = false)
    @JsonBackReference
    private OrdersBase ordersBase;

    @Column(name = "prd_nm", nullable = false)
    private String prdNm;

    @Column(name = "opt_val")
    private String optVal;
}
