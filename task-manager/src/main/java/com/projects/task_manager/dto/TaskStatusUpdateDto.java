package com.projects.task_manager.dto;

import com.projects.task_manager.entity.type.TaskStatusType;
import lombok.Data;

import java.util.UUID;

@Data
public class TaskStatusUpdateDto {
    private UUID taskId;
    private TaskStatusType taskStatus;
}