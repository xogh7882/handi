package com.handi.backend.repository;

import com.handi.backend.entity.Consultations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationsRepository extends JpaRepository<Consultations, Integer> {
}
