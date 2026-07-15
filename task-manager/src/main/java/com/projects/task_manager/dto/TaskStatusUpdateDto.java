package com.projects.task_manager.dto;

import com.projects.task_manager.entity.type.TaskStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TaskStatusUpdateDto {
    private UUID taskId;
    @NotNull(message = "Task status cannot be null")
    private TaskStatusType taskStatus;
}