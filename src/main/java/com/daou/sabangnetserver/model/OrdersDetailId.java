package com.daou.sabangnetserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrdersDetailId implements Serializable {

    @Column(name = "ord_prd_no")
    private int ordPrdNo;

    @Column(name = "ord_no")
    private String ordNo;
}

