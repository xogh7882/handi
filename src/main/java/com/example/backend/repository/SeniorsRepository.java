package com.example.backend.repository;

import com.example.backend.entity.Seniors;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeniorsRepository extends JpaRepository<Seniors, Integer> {
}
