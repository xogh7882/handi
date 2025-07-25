package com.handi.backend.repository;

import com.handi.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    Users findByEmail(String email);

    Users findByEmailAndSocialProvider(String email, String socialProvider);

}
