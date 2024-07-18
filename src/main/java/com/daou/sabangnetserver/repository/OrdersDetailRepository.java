package com.daou.sabangnetserver.repository;

import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.model.OrdersDetailId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrdersDetailRepository extends JpaRepository<OrdersDetail, OrdersDetailId> {


    // OrdersDetail 데이터를 ordersBase와 함께 가져오는 JPQL 쿼리
    // 페이징 요청 정보를 받아 Page<OrdersDetail> 타입의 결과를 반환
    @Query("SELECT od FROM OrdersDetail od JOIN FETCH od.ordersBase")
    Page<OrdersDetail> findAllWithOrdersBase(Pageable pageable);
}
