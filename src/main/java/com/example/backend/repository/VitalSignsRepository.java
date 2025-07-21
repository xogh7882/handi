package com.example.backend.repository;

import com.example.backend.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VitalSignsRepository extends JpaRepository<VitalSigns, Integer> {
}
