package com.handi.backend.entity;

import com.handi.backend.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "seniors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seniors {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 환자이름
    @Column(nullable = false)
    private String name;

    // 생년월일
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    // 성별
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // 입소일자
    @Column(name = "admission_date")
    private LocalDate admissionDate;

    // 퇴소일자
    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    // 환자 메모
    private String note;

    // 입소일 default
    @PrePersist
    protected void onCreate() {
        admissionDate = LocalDate.now();
    }
}