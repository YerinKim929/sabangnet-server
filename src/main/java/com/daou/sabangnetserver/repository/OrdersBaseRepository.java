package com.daou.sabangnetserver.repository;

import com.daou.sabangnetserver.model.OrdersBase;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface OrdersBaseRepository extends JpaRepository<OrdersBase, String> {
    List<OrdersBase> findBySellerNo(int sellerNo);
}
