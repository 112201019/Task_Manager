package com.projects.task_manager.repository;

import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String Email);
    Optional<Users> findByEmailOrUsername(String email, String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
