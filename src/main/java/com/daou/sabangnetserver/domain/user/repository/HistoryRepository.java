package com.daou.sabangnetserver.domain.user.repository;

import com.daou.sabangnetserver.domain.user.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("SELECT h " +
            "FROM History h " +
            "JOIN FETCH h.user " +
            "WHERE (:id = '' OR h.user.id = :id) AND " +
            "(:name = '' OR h.user.name LIKE %:name%)")
    Page<History> searchHistories(
            @Param("id") String id,
            @Param("name") String name,
            Pageable pageable
    );


}
