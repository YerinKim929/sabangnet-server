package com.daou.sabangnetserver.domain.user.entity;

import com.daou.sabangnetserver.domain.user.dto.UserUpdateMeRequestDto;
import com.daou.sabangnetserver.domain.user.dto.UserUpdateOthersRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USERS")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name ="ID", nullable = false)
    private String id;

    @Column(name = "PASSWORD", nullable = false)
    private String pw;

    @Column(name ="NAME", nullable = false)
    private String name;

    @Column(name ="EMAIL", nullable = false)
    private String email;

    @Column(name = "DEPARTMENT", nullable = true)
    private String department;

    @Column(name ="MEMO", nullable = true)
    private String memo;

    @Column(name ="REGISTRATION_DATE", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name ="IS_USED", nullable = false)
    private Boolean isUsed;

    @Column(name ="LAST_LOGIN_TIME", nullable = true)
    private LocalDateTime lastLoginTime;

    @Column(name ="LAST_LOGIN_IP", nullable = true)
    private String lastLoginIp;

    @Column(name="IS_DELETE", nullable = false)
    private Boolean isDelete;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
    private Set<Authority> authorities;

    public void updateLastLoginInfo(String lastLoginIp, LocalDateTime lastLoginTime){
        this.lastLoginTime = lastLoginTime;
        this.lastLoginIp = lastLoginIp;
    }

    public void updateIsUsed() {
        if (!this.getIsUsed()) {
            this.isUsed = true;
        }
    }

    public void updateUserInfo(UserUpdateOthersRequestDto requestDto) {
        this.name = requestDto.getName();
        this.email = requestDto.getEmail();
        this.department = requestDto.getDepartment();
        this.memo = requestDto.getMemo();
        this.authorities.clear();
        this.authorities.add(Authority.builder()
                .authorityName(requestDto.getAuthority().equals("MASTER") ? "ROLE_MASTER" : "ROLE_ADMIN")
                .build());
    }

    public void updateUserInfo(UserUpdateMeRequestDto requestDto){
        this.name = requestDto.getName();
        this.email = requestDto.getEmail();
        this.department = requestDto.getDepartment();
        this.memo = requestDto.getMemo();
    }

    public void deleteUser(){
        this.isUsed = false;
        this.isDelete = true;
    }

    public void updatePassword(String newPw){
        this.pw = newPw;
    }
}
