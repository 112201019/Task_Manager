package com.projects.task_manager.controller;


import com.projects.task_manager.dto.TaskDto;
import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.repository.TasksRepository;
import com.projects.task_manager.repository.UsersRepository;
import com.projects.task_manager.service.TasksServiceInterface;
import com.projects.task_manager.service.UserServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceInterface usersService;


    //implement adminDashboard, deleteanyuser, deleteanytask for now
    //should add admin dashboard button
    @PostMapping("/delete-user/{userId}")
    public String deleteUserAsAdmin(@PathVariable Long userId) {
        usersService.deleteUser(userId);
        return "redirect:/admin/dashboard";
    }

    private final TasksServiceInterface tasksService;
    @GetMapping("/dashboard")
    public String adminDashboard(Model model){
        List<UserDto> allUsers = usersService.getAllUsers();
        List<TaskDto> allTasks = tasksService.getAllTasksAdmin();
        model.addAttribute("usersList", allUsers);
        model.addAttribute("tasksList", allTasks);
        return "admin-dashboard";
    }

    @PostMapping("/delete-task/{taskId}")
    public String deleteTaskAsAdmin(@PathVariable Long taskId) {
        tasksService.deleteTask(taskId);
        return "redirect:/admin/dashboard";
    }


}
