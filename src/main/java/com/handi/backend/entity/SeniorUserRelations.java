package com.handi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "senior_user_relations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeniorUserRelations {
    // 복합키
    @EmbeddedId
    private SeniorUserRelationsId id;

    // 생성일자
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 수정시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seniorId")
    @JoinColumn(name = "senior_id")
    private Seniors senior;

    // 생성일자 default
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 수정일자 default
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}