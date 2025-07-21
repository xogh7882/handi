package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organizations {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 기관명
    @Column(nullable = false)
    private String name;

    // 아침시간
    @Column(name = "breakfast_time")
    private LocalTime breakfastTime;
    
    // 점심시간
    @Column(name = "lunch_time")
    private LocalTime lunchTime;

    // 저녁시간
    @Column(name = "dinner_time")
    private LocalTime dinnerTime;

    // 취침시간
    @Column(name = "sleep_time")
    private LocalTime sleepTime;
}
