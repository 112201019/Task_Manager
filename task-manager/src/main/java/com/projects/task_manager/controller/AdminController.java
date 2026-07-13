package com.projects.task_manager.controller;


import com.projects.task_manager.dto.EditUserDto;
import com.projects.task_manager.dto.TaskDto;
import com.projects.task_manager.dto.TaskRequestDto;
import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.service.TasksServiceInterface;
import com.projects.task_manager.service.UserServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.management.DescriptorKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserServiceInterface usersService;
    private final TasksServiceInterface tasksService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Entered /admin/dashboard api in admincontroller");

        // Fetch paginated results
        Page<UserDto> usersPage = usersService.getAllUsers(page, size);
        Page<TaskDto> tasksPage = tasksService.getAllTasksAdmin(page, size);

        Map<String, Object> response = new HashMap<>();

        // .getContent() extracts the raw list so frontend data.users.forEach() still works flawlessly
        response.put("users", usersPage.getContent());
        response.put("tasks", tasksPage.getContent());

        // Expose pagination metadata for the frontend to build "Next/Prev" buttons
        response.put("usersCurrentPage", usersPage.getNumber());
        response.put("usersTotalPages", usersPage.getTotalPages());
        response.put("tasksCurrentPage", tasksPage.getNumber());
        response.put("tasksTotalPages", tasksPage.getTotalPages());

        log.info("Dashboard loaded with pagination data.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<Void> deleteUserAsAdmin(@PathVariable UUID userId) {
        log.info("attempting to delete user:{} using /delete-user api in admincontroller", userId);
        usersService.deleteUser(userId);
        log.info("user: {} deleted successfully", userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @DeleteMapping("/delete-task/{taskId}")
    public ResponseEntity<Void> deleteTaskAsAdmin(@PathVariable UUID taskId) {
        log.info("attempting to delete task:{} using /delete-user api in admincontroller", taskId);
        tasksService.deleteTask(taskId);
        log.info("task: {} deleted successfully", taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/edit-user/{userId}")
    public ResponseEntity<Void> editUserAsAdmin(@PathVariable UUID userId, @RequestBody EditUserDto editRequest) {
        log.info("attempting to edit user:{} using /delete-user api in admincontroller", userId);
        editRequest.setUserId(userId);
        usersService.editUser(editRequest);
        log.info("user: {} edited successfully", userId);
        return ResponseEntity.ok().build();
    }
}
