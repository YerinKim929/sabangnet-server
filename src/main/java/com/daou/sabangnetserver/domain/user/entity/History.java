package com.daou.sabangnetserver.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HISTORY")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HISTORY_ID", nullable = false)
    private Long historyId;

    @Column(name ="LOGIN_TIME", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "LOGIN_DEVICE", nullable = false)
    private String loginDevice;

    @Column(name ="LOGIN_IP", nullable = false)
    private String loginIp;

    @ManyToOne
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", updatable = false)
    private User user;
}
