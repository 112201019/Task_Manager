package com.projects.task_manager.dto;

import com.projects.task_manager.entity.type.TaskPriority;
import com.projects.task_manager.entity.type.TaskStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class TaskDto {
    private Long taskId;
    private String title;
    private String description;
    private TaskPriority taskPriority;
    private TaskStatusType taskStatus;
    private LocalDateTime dueDate;
    private Long userId;
}
