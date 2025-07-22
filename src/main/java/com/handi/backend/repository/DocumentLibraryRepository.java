package com.handi.backend.repository;

import com.handi.backend.entity.DocumentLibrary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentLibraryRepository extends JpaRepository<DocumentLibrary, Long> {
}
