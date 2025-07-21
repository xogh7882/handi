package com.example.backend.entity;

import com.example.backend.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "사용자")
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Schema(description = "PK")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 소속기관
    @Schema(description = "소속기관")
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    // email
    @Schema(description = "email")
    @Column(nullable = false)
    private String email;

    // 이름
    @Schema(description = "이름")
    @Column(nullable = false)
    private String name;

    // 소셜 프로바이더 ( google, kakao, naver 등 )
    @Schema(description = "소셜 프로바이더")
    @Column(name = "social_provider", nullable = false)
    private String socialProvider;

    // Role ( EMPLOYEE, ADMIN, GUARDIAN )
    @Schema(description = "역할")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 전화번호
    @Schema(description = "전화번호")
    private String phone;

    // 주소
    @Schema(description = "주소")
    private String address;

    // 생성일자
    @Schema(description = "생성일자")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Access Token
    @Schema(description = "Access Token")
    @Column(name = "access_token")
    private String accessToken;

    // default = now();
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}