package com.example.back_end.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.back_end.models.Role;
public interface RoleReposiroty extends JpaRepository<Role, Long> {
}
