package com.projects.task_manager.controller;

import com.projects.task_manager.dto.TaskRequestDto;
import com.projects.task_manager.dto.TaskDto;
import com.projects.task_manager.dto.TaskStatusUpdateDto;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.entity.type.TaskPriority;
import com.projects.task_manager.entity.type.TaskStatusType;
import com.projects.task_manager.service.implementations.TasksService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
@RestController
@RequestMapping("api/tasks")
@RequiredArgsConstructor
public class TasksController {
    private final TasksService tasksService;


    @PostMapping("/save")
    public ResponseEntity<Void> createTask(@Valid @RequestBody TaskRequestDto request, @AuthenticationPrincipal Users currentUser){
        //CRUD operations Create- Read- Update Delete
        request.setTaskStatus(TaskStatusType.TO_DO);
        tasksService.createNewTask(request, currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<TaskDto>> readTasks(@AuthenticationPrincipal Users currentUser){
        UUID user_id = (UUID) currentUser.getUserId();
        List<TaskDto> tasks = tasksService.getAllTasks(user_id);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/edit/{task_id}")
    public ResponseEntity<Void> updateTask(@AuthenticationPrincipal Users currentUser, @PathVariable UUID task_id, @Valid @RequestBody TaskRequestDto request){
        if(currentUser.getUserId().equals(tasksService.fetchTask(task_id).getUserId())){
            TaskDto t = tasksService.fetchTask(task_id);
            request.setTaskStatus(t.getTaskStatus());
            tasksService.updateTask(task_id, request);
            return ResponseEntity.ok().build();
        }
        else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/delete/{task_id}")
    public ResponseEntity<Void> deleteStudent(@AuthenticationPrincipal Users currentUser, @PathVariable UUID task_id){
        if(currentUser.getUserId().equals(tasksService.fetchTask(task_id).getUserId())){
            tasksService.deleteTask(task_id);
            return ResponseEntity.noContent().build();
        }
        else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/update-status/{task_id}")
    public ResponseEntity<Void> updateStatus(@AuthenticationPrincipal Users currentUser, @Valid @RequestBody TaskStatusUpdateDto taskStatus, @PathVariable UUID task_id){
        if(currentUser.getUserId().equals(tasksService.fetchTask(task_id).getUserId())){
            TaskDto existingTask = tasksService.fetchTask(task_id);
            TaskRequestDto taskRequest = new TaskRequestDto();
            taskRequest.setTitle(existingTask.getTitle());
            taskRequest.setDescription(existingTask.getDescription());
            taskRequest.setTaskPriority(existingTask.getTaskPriority());
            taskRequest.setTaskStatus(taskStatus.getTaskStatus());
            taskRequest.setRecurring(existingTask.isRecurring());
            taskRequest.setDueDate(existingTask.getDueDate());
            tasksService.updateTask(task_id, taskRequest);
            return ResponseEntity.ok().build();
        }
        else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


    //removed forms for decoupling the view and controller (Should write a UI model seperately??)
//    @GetMapping("/new")
//    public String Taskform(Model model, @AuthenticationPrincipal Users currentUser){
//        TaskRequestDto taskRequestDto = new TaskRequestDto();
//        taskRequestDto.setUserId(currentUser.getUserId());
//        model.addAttribute("taskRequest", taskRequestDto);
//        model.addAttribute("priorities", TaskPriority.values());
//        return "task-form";
//    }


//    @GetMapping("/edit/fetch/{taskId}") //this is a form again
//    public String fetchUpdateForm(@PathVariable Long taskId, Model model){
//        TaskDto existingTask = tasksService.fetchTask(taskId);
//        TaskRequestDto taskRequest = new TaskRequestDto();
//        taskRequest.setTitle(existingTask.getTitle());
//        taskRequest.setDescription(existingTask.getDescription());
//        taskRequest.setTaskPriority(existingTask.getTaskPriority());
//        taskRequest.setTaskStatus(existingTask.getTaskStatus());
//        taskRequest.setDueDate(existingTask.getDueDate());
//        taskRequest.setUserId(existingTask.getUserId());
//
//        model.addAttribute("taskRequest", taskRequest);
//        model.addAttribute("taskId", taskId); // Pass the ID explicitly to track which task to update
//        model.addAttribute("priorities", TaskPriority.values()); // Using clean controller enum approach
//        return "task-edit-form";

//    }
}
