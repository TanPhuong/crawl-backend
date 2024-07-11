package com.example.back_end.repository;

import com.example.back_end.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
}
