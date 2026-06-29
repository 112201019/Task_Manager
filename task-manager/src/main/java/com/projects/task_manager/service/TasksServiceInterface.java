package com.projects.task_manager.service;

import com.projects.task_manager.dto.TaskRequestDto;
import com.projects.task_manager.dto.TaskDto;

import java.util.List;

public interface TasksServiceInterface {

    void createNewTask(TaskRequestDto request); //task creation

    List<TaskDto> getAllTasks(Long userId); //get all non deleted tasks

    void updateTask(Long id, TaskRequestDto requestDto); //updates a selected task, task modification and status

    void deleteTask(Long id); //delete task

    TaskDto fetchTask(Long id);
}
