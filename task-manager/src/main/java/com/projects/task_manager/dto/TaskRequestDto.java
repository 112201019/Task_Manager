package com.projects.task_manager.dto;

import com.projects.task_manager.entity.type.TaskPriority;
import com.projects.task_manager.entity.type.TaskStatusType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskRequestDto {

    @NotBlank(message = "Task title cannot be empty") // NEW
    private String title;

    private String description;
    private TaskPriority taskPriority;
    private TaskStatusType taskStatus;
    private boolean isRecurring;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDate;
}
