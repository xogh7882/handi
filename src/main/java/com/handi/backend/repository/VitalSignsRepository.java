package com.handi.backend.repository;

import com.handi.backend.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VitalSignsRepository extends JpaRepository<VitalSigns, Integer> {
}
