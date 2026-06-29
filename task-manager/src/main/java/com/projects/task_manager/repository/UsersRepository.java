package com.projects.task_manager.repository;

import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String Email);

}
