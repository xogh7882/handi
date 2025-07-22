package com.handi.backend.repository;

import com.handi.backend.entity.SeniorUserRelations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeniorUserRelationsRepository extends JpaRepository<SeniorUserRelations, Integer> {
    @Query("SELECT sur.senior.id FROM SeniorUserRelations sur WHERE sur.user.id = :userId")
    List<Integer> findSeniorIdsByUserId(@Param("userId") Integer userId);
}
