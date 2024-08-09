package com.daou.sabangnetserver.domain.user.repository;

import com.daou.sabangnetserver.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE " +
            "(:id = '' OR u.id = :id) AND " +
            "(:name = '' OR u.name LIKE %:name%) AND " +
            "(:email = '' OR u.email LIKE %:email%) AND " +
            "(:isUsed IS NULL OR u.isUsed = :isUsed) AND " +
            "(isDelete = FALSE)"
    )
    Page<User> searchUsers(
            @Param("id") String id,
            @Param("name") String name,
            @Param("email") String email,
            @Param("isUsed") Boolean isUsed,
            Pageable pageable
    );

    Optional<User> findByIdAndIsDeleteFalse(String id);
    Optional<User> findOneWithAuthoritiesByIdAndIsDeleteFalse(String id);

    boolean existsByIdAndIsDeleteFalse(String id);
    boolean existsByEmailAndIsDeleteFalse(String email);
}

