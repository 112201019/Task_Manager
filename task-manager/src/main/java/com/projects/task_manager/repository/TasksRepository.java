package com.projects.task_manager.repository;

import com.projects.task_manager.entity.Tasks;
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

public interface TasksRepository extends JpaRepository<Tasks, UUID> {

    @Query("SELECT t FROM Tasks t WHERE t.taskId = :id AND t.taskStatus != 'DELETED'")
    Optional<Tasks> findById(@Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM Tasks t WHERE t.taskId = :id")
    void deleteById(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tasks t WHERE t.taskId = :id")
    boolean existsById(@Param("id") UUID id);

    @Query("SELECT t FROM Tasks t WHERE t.taskStatus != 'DELETED'")
    Page<Tasks> getAllTasks(Pageable pageable);
}
