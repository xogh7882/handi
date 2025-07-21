package com.example.backend.entity;

import com.example.backend.enums.ConsultationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Consultations {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 확정 스케줄 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_schedule_match_id", nullable = false)
    private AutoScheduleMatches autoScheduleMatch;

    // 상담 내용 정리 ( whisper AI 사용 )
    private String content;

    // 화상 회의 코드
    private String code;

    // 생성 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 상태 ( PENDING, CONDUCTED, CANCELED )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationStatus status;

    // 생성시각 default
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 수정시각 default
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}