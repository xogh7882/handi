package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vital_signs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalSigns {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 환자 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_id", nullable = false)
    private Seniors senior;

    // 최고 혈압
    private Integer systolic;

    // 최저 혈압
    private Integer diastolic;

    // 혈당
    @Column(name = "blood_glucose")
    private Integer bloodGlucose;

    // 체온
    @Column(precision = 4, scale = 1)
    private BigDecimal temperature;

    // 키
    @Column(precision = 4, scale = 1)
    private BigDecimal height;

    // 몸무게
    @Column(precision = 4, scale = 1)
    private BigDecimal weight;

    // 측정일자
    @Column(name = "measured_date")
    private LocalDate measuredDate;

    // 생성시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 생성시각 default
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}