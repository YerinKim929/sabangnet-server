package com.daou.sabangnetserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrdersDetailId implements Serializable {

    @Column(name = "ord_prd_no")
    private int ordPrdNo;

    @Column(name = "ord_no")
    private Long ordNo;
}

