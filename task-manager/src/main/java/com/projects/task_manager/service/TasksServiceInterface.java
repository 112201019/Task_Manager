package com.projects.task_manager.service;

import com.projects.task_manager.dto.TaskRequestDto;
import com.projects.task_manager.dto.TaskDto;

import java.util.List;
import java.util.UUID;

public interface TasksServiceInterface {

    void createNewTask(TaskRequestDto request); //task creation

    List<TaskDto> getAllTasks(UUID userId); //get all non deleted tasks

    void updateTask(UUID id, TaskRequestDto requestDto); //updates a selected task, task modification and status

    void deleteTask(UUID id); //delete task

    TaskDto fetchTask(UUID id);

    List<TaskDto> getAllTasksAdmin();
}
