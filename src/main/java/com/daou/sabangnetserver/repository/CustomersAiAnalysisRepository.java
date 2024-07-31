package com.daou.sabangnetserver.repository;

import com.daou.sabangnetserver.model.CustomersAiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomersAiAnalysisRepository extends JpaRepository<CustomersAiAnalysis, Long> {
    Optional<CustomersAiAnalysis> findByNameAndPhoneNumber(String name, String phoneNumber);
}