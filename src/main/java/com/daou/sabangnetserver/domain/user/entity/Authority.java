package com.daou.sabangnetserver.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "AUTHORITY")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUTHORITY_ID", nullable = false)
    private Long id;

    @Column(name = "AUTHORITY_NAME", length = 50, nullable = false)
    private String authorityName;

    public Authority(String authorityName) {
        this.authorityName = authorityName;
    }
}
