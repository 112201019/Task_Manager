package com.projects.task_manager.repository;

import com.projects.task_manager.entity.Tasks;
import com.projects.task_manager.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TasksRepository extends JpaRepository<Tasks, Long> {

    @Query(value = "SELECT * FROM tasks", nativeQuery = true)
    List<Tasks> getAllTasks();

//    @Query(value = "SELECT * FROM users WHERE role = :user_role", nativeQuery = true)
//    List<User> findUsersByRole(String user_role);
}
