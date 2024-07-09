package com.daou.sabangnetserver.domain.user.repository;

import com.daou.sabangnetserver.domain.user.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
}
