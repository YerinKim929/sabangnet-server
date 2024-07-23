package com.daou.sabangnetserver.repository;

import com.daou.sabangnetserver.model.OrdersBase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdersBaseRepository extends JpaRepository<OrdersBase, Long> {

    @EntityGraph(attributePaths = "ordersDetail")
    @Query("SELECT ob FROM OrdersBase ob LEFT JOIN FETCH ob.ordersDetail WHERE ob.ordNo IN :ordNos")
    List<OrdersBase> findAllByIdWithDetails(@Param("ordNos") List<Long> ordNos);


    List<OrdersBase> findBySellerNo(int sellerNo);
}
