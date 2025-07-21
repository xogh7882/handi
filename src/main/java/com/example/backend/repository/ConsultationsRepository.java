package com.example.backend.repository;

import com.example.backend.entity.Consultations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationsRepository extends JpaRepository<Consultations, Integer> {
}
