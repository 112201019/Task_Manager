package com.projects.task_manager.repository;

import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {

    @Query("SELECT u FROM Users u WHERE u.userId = :id AND u.deleted = false")
    Optional<Users> findById(@Param("id") UUID id);

    @Query("SELECT u FROM Users u WHERE u.deleted = false")
    Page<Users> findAll(Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE u.userId = :id AND u.deleted = false")
    boolean existsById(@Param("id") UUID id);

    @Query("SELECT u FROM Users u WHERE LOWER(u.email) = LOWER(:email) AND u.deleted = false")
    Optional<Users> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM Users u WHERE LOWER(u.username) = LOWER(:username) AND u.deleted = false")
    Optional<Users> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM Users u WHERE (LOWER(u.email) = LOWER(:identifier) OR LOWER(u.username) = LOWER(:identifier)) AND u.deleted = false")
    Optional<Users> findByEmailOrUsername(@Param("identifier") String identifier);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE LOWER(u.username) = LOWER(:username) AND u.deleted = false")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE LOWER(u.email) = LOWER(:email) AND u.deleted = false")
    boolean existsByEmail(@Param("email") String email);

    // Hard-delete query for the Cron Job
    @Modifying
    @Query("DELETE FROM Users u WHERE u.deleted = true AND u.deletedAt < :cutoffDate")
    void deleteExpiredUsers(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
