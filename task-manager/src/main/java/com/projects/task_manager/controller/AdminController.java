package com.projects.task_manager.controller;


import com.projects.task_manager.dto.TaskDto;
import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.service.TasksServiceInterface;
import com.projects.task_manager.service.UserServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.management.DescriptorKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/admin") // 2. Standardized base route
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 3. THE LOCK: Only Admins allowed past this point!
public class AdminController {

    private final UserServiceInterface usersService;
    private final TasksServiceInterface tasksService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard(){
        List<UserDto> allUsers = usersService.getAllUsers();
        List<TaskDto> allTasks = tasksService.getAllTasksAdmin();
        Map<String, Object> response = new HashMap<>();
        response.put("users", allUsers);
        response.put("tasks", allTasks);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<Void> deleteUserAsAdmin(@PathVariable Long userId) {
        usersService.deleteUser(userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @DescriptorKey("/delete-task/{taskId}")
    public ResponseEntity<Void> deleteTaskAsAdmin(@PathVariable Long taskId) {
        tasksService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }


}
