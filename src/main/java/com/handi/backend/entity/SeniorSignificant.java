package com.handi.backend.entity;

import com.handi.backend.enums.Level;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "senior_significant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeniorSignificant {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 환자 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_id", nullable = false)
    private Seniors senior;

    // 내용
    private String content;

    // 중요도 ( HIGH, MEDIUM, LOW )
    @Enumerated(EnumType.STRING)
    private Level level;

    // 생성 시간
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 생성 시간 default
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}