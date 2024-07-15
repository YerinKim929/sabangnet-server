package com.daou.sabangnetserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrdersBase {

    // 주문 번호 (기본키)
    @Id
    @Column(nullable = false, unique = true)
    private int ordNo;

    // 주문 접수 일시
    @Column(nullable = false)
    private LocalDateTime ordDttm;

    // 수취인 이름
    @Column(nullable = false, length = 255)
    private String rcvrNm;

    // 배송지 주소
    @Column(nullable = false, length = 255)
    private String rcvrAddr;

    // 배송지 연락처
    @Column(nullable = false, length = 20)
    private String rcvrMphnNo;

    // 판매자 번호 (고정 값)
    @Column(nullable = false)
    private Integer sellerNo;

    // 주문 수집 일시
    @Column(nullable = false)
    private LocalDateTime ordCollectDttm;

    // 일대다 매핑
    @OneToMany(mappedBy = "ordersBase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrdersDetail> ordersDetail;

}
