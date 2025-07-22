package com.handi.backend.repository;

import com.handi.backend.entity.Organizations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationsRepository extends JpaRepository<Organizations, Integer> {
}
