package com.handi.backend.entity;

import com.handi.backend.enums.MedicationSchedule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medications {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 투약 스케줄 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_schedules_id", nullable = false)
    private MedicationSchedules medicationSchedules;

    // 투약 이미지
    @Column(name = "medication_photo_path")
    private String medicationPhotoPath;

    // 투약 시간
    @Column(name = "medicated_at")
    private LocalDateTime medicatedAt;

    // 시간 구분 ( 7가지 )
    @Enumerated(EnumType.STRING)
    @Column(name = "medication_schedule", nullable = false)
    private MedicationSchedule medicationSchedule;
}