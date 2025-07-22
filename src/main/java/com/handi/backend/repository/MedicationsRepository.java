package com.handi.backend.repository;

import com.handi.backend.entity.Medications;
import com.handi.backend.enums.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicationsRepository extends JpaRepository<Medications, Integer> {
    @Query("SELECT m FROM Medications m WHERE m.medicationSchedules.senior.id = :seniorId AND m.medicationSchedule = :schedule")
    List<Medications> findBySeniorIdAndSchedule(@Param("seniorId") Integer seniorId, @Param("schedule") MedicationSchedule schedule);
}
