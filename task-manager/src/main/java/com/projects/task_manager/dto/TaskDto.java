package com.projects.task_manager.dto;

import com.projects.task_manager.entity.type.TaskPriority;
import com.projects.task_manager.entity.type.TaskStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class TaskDto {
    private UUID taskId;
    private String title;
    private String description;
    private TaskPriority taskPriority;
    private TaskStatusType taskStatus;
    private LocalDateTime dueDate;
    private UUID userId;
}
