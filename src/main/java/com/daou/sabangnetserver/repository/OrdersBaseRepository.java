package com.daou.sabangnetserver.repository;

import com.daou.sabangnetserver.model.OrdersBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersBaseRepository extends JpaRepository<OrdersBase, Long> {
    Optional<List<OrdersBase>> findBySellerNo(int sellerNo);
}
