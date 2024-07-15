package com.daou.sabangnetserver.repository;

import com.daou.sabangnetserver.model.OrdersBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdersBaseRepository extends JpaRepository<OrdersBase, Long> {
    List<OrdersBase> findAllByOrdDttmBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
