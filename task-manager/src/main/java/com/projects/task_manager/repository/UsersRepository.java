package com.projects.task_manager.repository;

import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {

    @Query("SELECT u FROM Users u WHERE u.userId = :id")
    Optional<Users> findById(@Param("id") UUID id);

    @Query("SELECT u FROM Users u")
    List<Users> findAll();

    @Modifying
    @Query("DELETE FROM Users u WHERE u.userId = :id")
    void deleteById(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE u.userId = :id")
    boolean existsById(@Param("id") UUID id);

    @Query("SELECT u FROM Users u WHERE u.email = :email")
    Optional<Users> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM Users u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<Users> findByEmailOrUsername(@Param("identifier") String email, @Param("identifier") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
