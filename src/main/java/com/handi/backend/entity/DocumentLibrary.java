package com.handi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_library")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentLibrary {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 환자 PK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_id", nullable = false)
    private Seniors senior;

    // 문서 이름
    @Column(name = "document_name")
    private String documentName;

    // 문서 이미지 링크
    @Column(name = "original_photo_paths")
    private String originalPhotoPaths; // TEXT[] -> String으로 변환 (JSON 형태로 저장)

    // 업르드 시간
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}