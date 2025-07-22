package com.handi.backend.repository;

import com.handi.backend.entity.MedicationSchedules;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationSchedulesRepository extends JpaRepository<MedicationSchedules, Long> {
}
