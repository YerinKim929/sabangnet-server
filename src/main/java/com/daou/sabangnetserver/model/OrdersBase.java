package com.daou.sabangnetserver.model;

import com.daou.sabangnetserver.model.OrdersDetail;
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
@Table(name = "orders_base")
public class OrdersBase {

    @Id
    @Column(name = "ord_no", nullable = false, unique = true)
    private Long ordNo;

    @Column(name = "ord_dttm", nullable = false)
    private LocalDateTime ordDttm;

    @Column(name = "rcvr_nm", nullable = false, length = 255)
    private String rcvrNm;

    @Column(name = "rcvr_addr", nullable = false, length = 255)
    private String rcvrAddr;

    @Column(name = "rcvr_mphn_no", nullable = false, length = 20)
    private String rcvrMphnNo;

    @Column(name = "seller_no", nullable = false)
    private Integer sellerNo;

    @Column(name = "ord_collect_dttm", nullable = false)
    private LocalDateTime ordCollectDttm;

    @OneToMany(mappedBy = "ordersBase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrdersDetail> ordersDetail;
}
