package com.handi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auto_schedule_matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoScheduleMatches {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 사용자 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 환자 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_id", nullable = false)
    private Seniors senior;

    // 스케줄 확정 날짜
    @Column(name = "meeting_date")
    private LocalDateTime meetingDate;
}