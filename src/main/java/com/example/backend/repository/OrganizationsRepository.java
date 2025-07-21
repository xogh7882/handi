package com.example.backend.repository;

import com.example.backend.entity.Organizations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationsRepository extends JpaRepository<Organizations, Integer> {
}
