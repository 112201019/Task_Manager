package com.projects.task_manager.dto;

import com.projects.task_manager.entity.type.TaskPriority;
import com.projects.task_manager.entity.type.TaskStatusType;
import lombok.Data;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskRequestDto {
    private String title;
    private String description;
    private TaskPriority taskPriority;
    private TaskStatusType taskStatus;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDate;
    private UUID userId;
}
