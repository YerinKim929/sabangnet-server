package com.daou.sabangnetserver.repository;

import com.daou.sabangnetserver.model.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TokensRepository extends JpaRepository<Tokens, Integer> {
}

