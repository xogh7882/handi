package com.example.backend.repository;

import com.example.backend.entity.DocumentLibrary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentLibraryRepository extends JpaRepository<DocumentLibrary, Long> {
}
