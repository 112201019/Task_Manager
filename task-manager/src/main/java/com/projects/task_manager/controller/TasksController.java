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

@RestController
@RequestMapping("api/tasks")
@RequiredArgsConstructor
public class TasksController {
    private final TasksService tasksService;


    @PostMapping("/save")
    public ResponseEntity<Void> createTask(@RequestBody TaskRequestDto request, @AuthenticationPrincipal Users currentUser){
        //CRUD operations Create- Read- Update Delete
        request.setUserId(currentUser.getUserId());
        request.setTaskStatus(TaskStatusType.TO_DO);
        tasksService.createNewTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<TaskDto>> readTasks(@AuthenticationPrincipal Users currentUser){
        Long user_id = (Long) currentUser.getUserId();
        List<TaskDto> tasks = tasksService.getAllTasks(user_id);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/edit/{task_id}")
    public ResponseEntity<Void> updateTask(@PathVariable Long task_id, @RequestBody TaskRequestDto request){
        TaskDto t = tasksService.fetchTask(task_id);
        request.setTaskStatus(t.getTaskStatus());
        request.setUserId(t.getUserId());
        tasksService.updateTask(task_id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{task_id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long task_id){
        tasksService.deleteTask(task_id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-status/{task_id}")
    public ResponseEntity<Void> updateStatus(@RequestBody TaskStatusUpdateDto taskStatus, @PathVariable Long task_id){
        TaskDto existingTask = tasksService.fetchTask(task_id);
        TaskRequestDto taskRequest = new TaskRequestDto();
        taskRequest.setTitle(existingTask.getTitle());
        taskRequest.setDescription(existingTask.getDescription());
        taskRequest.setTaskPriority(existingTask.getTaskPriority());
        taskRequest.setTaskStatus(taskStatus.getTaskStatus());
        taskRequest.setDueDate(existingTask.getDueDate());
        taskRequest.setUserId(existingTask.getUserId());
        tasksService.updateTask(task_id, taskRequest);
        return ResponseEntity.ok().build();
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
