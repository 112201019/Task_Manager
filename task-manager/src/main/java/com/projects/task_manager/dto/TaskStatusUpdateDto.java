package com.projects.task_manager.dto;

import com.projects.task_manager.entity.type.TaskStatusType;
import lombok.Data;

@Data
public class TaskStatusUpdateDto {
    private Long taskId;
    private TaskStatusType taskStatus;
}