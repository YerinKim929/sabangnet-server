package com.daou.sabangnetserver.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data

//복합키 설정
public class OrdersDetailId implements Serializable {

    private int ordPrdNo;
    private int ordNo;

}

