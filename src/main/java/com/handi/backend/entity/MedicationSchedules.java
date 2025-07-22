package com.handi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicationSchedules {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 환자 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_id", nullable = false)
    private Seniors senior;

    // 약 이름
    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    // 투약 시작 날짜
    @Column(name = "medication_startdate")
    private String medicationStartdate;

    // 투약 종료 날짜
    @Column(name = "medication_enddate")
    private String medicationEnddate;

    // 생성 일자
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 주의사항
    private String description;

    // 약품정보
    @Column(name = "medication_info")
    private String medicationInfo;

    // 생성일자 default
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}