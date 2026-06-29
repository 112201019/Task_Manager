package com.projects.task_manager.controller;

import com.projects.task_manager.dto.TaskRequestDto;
import com.projects.task_manager.dto.TaskDto;
import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.entity.type.TaskPriority;
import com.projects.task_manager.entity.type.TaskStatusType;
import com.projects.task_manager.service.implementations.TasksService;
import com.projects.task_manager.service.implementations.UsersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/task")
@RequiredArgsConstructor
public class TasksController {
    private final TasksService tasksService;
    private final UsersService usersService;
    @GetMapping("/new")
    public String Taskform(Model model, @AuthenticationPrincipal Users currentUser){
        TaskRequestDto taskRequestDto = new TaskRequestDto();
        taskRequestDto.setUserId(currentUser.getUserId());
        model.addAttribute("taskRequest", taskRequestDto);
        model.addAttribute("priorities", TaskPriority.values());
        return "task-form";
    }

    @PostMapping("/save")
    public String createNewTask(@ModelAttribute("taskRequest") TaskRequestDto request){
        request.setTaskStatus(TaskStatusType.TO_DO);
        tasksService.createNewTask(request);
        return "redirect:/task/getall";
    }

    @GetMapping("/getall")
    public String getAllTasks(@AuthenticationPrincipal Users currentUser, Model model){
        Long user_id = (Long) currentUser.getUserId();

        UserDto freshUser = usersService.fetchUser(String.valueOf(user_id));
        model.addAttribute("currentUser", freshUser);

        List<TaskDto> tasks = tasksService.getAllTasks(user_id);
        model.addAttribute("tasksList", tasks);
        model.addAttribute("statuses", new TaskStatusType[]{TaskStatusType.TO_DO, TaskStatusType.DONE, TaskStatusType.IN_PROGRESS});
        model.addAttribute("userId", user_id);
        return "tasks";
    }

    @GetMapping("/edit/fetch/{taskId}")
    public String fetchUpdateForm(@PathVariable Long taskId, Model model){
        TaskDto existingTask = tasksService.fetchTask(taskId);
        TaskRequestDto taskRequest = new TaskRequestDto();
        taskRequest.setTitle(existingTask.getTitle());
        taskRequest.setDescription(existingTask.getDescription());
        taskRequest.setTaskPriority(existingTask.getTaskPriority());
        taskRequest.setTaskStatus(existingTask.getTaskStatus());
        taskRequest.setDueDate(existingTask.getDueDate());
        taskRequest.setUserId(existingTask.getUserId());

        model.addAttribute("taskRequest", taskRequest);
        model.addAttribute("taskId", taskId); // Pass the ID explicitly to track which task to update
        model.addAttribute("priorities", TaskPriority.values()); // Using clean controller enum approach
        return "task-edit-form";
    }

    @PostMapping("/edit/{task_id}")
    public String updateTask(@PathVariable Long task_id, @ModelAttribute("taskRequest") TaskRequestDto request){
        TaskDto t = tasksService.fetchTask(task_id);
        request.setTaskStatus(t.getTaskStatus());
        tasksService.updateTask(task_id, request);
        return "redirect:/task/getall";
    }

    @DeleteMapping("/delete/{task_id}")
    public String deleteStudent(@PathVariable Long task_id){
        tasksService.deleteTask(task_id);
        return "redirect:/task/getall";
    }

    @PostMapping("/update-status/{task_id}")
    public String updateStatus(@PathVariable Long task_id, @RequestParam TaskStatusType taskStatus){
        TaskDto existingTask = tasksService.fetchTask(task_id);
        TaskRequestDto taskRequest = new TaskRequestDto();
        taskRequest.setTitle(existingTask.getTitle());
        taskRequest.setDescription(existingTask.getDescription());
        taskRequest.setTaskPriority(existingTask.getTaskPriority());
        taskRequest.setTaskStatus(taskStatus);
        taskRequest.setDueDate(existingTask.getDueDate());
        taskRequest.setUserId(existingTask.getUserId());
        tasksService.updateTask(task_id, taskRequest);
        return "redirect:/task/getall";
    }
}
