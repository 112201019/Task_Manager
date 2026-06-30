package com.projects.task_manager.service.implementations;

import com.projects.task_manager.dto.TaskRequestDto;
import com.projects.task_manager.dto.TaskDto;
import com.projects.task_manager.entity.Tasks;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.entity.type.TaskStatusType;
import com.projects.task_manager.repository.TasksRepository;
import com.projects.task_manager.repository.UsersRepository;
import com.projects.task_manager.service.TasksServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime; // Make sure to add this import at the top
@Service
@RequiredArgsConstructor
public class TasksService implements TasksServiceInterface{

    private final UsersRepository UsersRepository;
    private final TasksRepository tasksRepository;

    //create new tasks
    @Transactional
    public void createNewTask(TaskRequestDto request){
        Long userId = request.getUserId();

        Users user = UsersRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Tasks newTask = Tasks.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .taskPriority(request.getTaskPriority())
                .taskStatus(request.getTaskStatus())
                .dueDate(request.getDueDate())
                .build();

        newTask.setUser(user);
        user.getTasks().add(newTask);

        newTask= tasksRepository.save(newTask);
        new TaskDto(
                newTask.getTaskId(),
                newTask.getTitle(),
                newTask.getDescription(),
                newTask.getTaskPriority(),
                newTask.getTaskStatus(),
                newTask.getDueDate(),
                newTask.getUser().getUserId()
        );
    }

    @Override
    public List<TaskDto> getAllTasks(Long userId) {
        Users user = UsersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        List<Tasks> t=user.getTasks();
        for(Tasks task : t){
            // If the due date has passed AND it's not already marked DONE or EXPIRED
            if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())
                    && task.getTaskStatus() != TaskStatusType.DONE
                    && task.getTaskStatus() != TaskStatusType.OVERDUE
                    && task.getTaskStatus() != TaskStatusType.DELETED) {
                task.setTaskStatus(TaskStatusType.OVERDUE);
                tasksRepository.save(task);
            }
        }

        return t.stream()
                .filter(task -> (task.getTaskStatus()!=TaskStatusType.DELETED))
                .map( k -> new TaskDto(
                        k.getTaskId(),
                        k.getTitle(),
                        k.getDescription(),
                        k.getTaskPriority(),
                        k.getTaskStatus(),
                        k.getDueDate(),
                        k.getUser().getUserId())
                ).toList();
    }

    @Override
    public void updateTask(Long id, TaskRequestDto requestDto) {
        Tasks task = tasksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found to update."));
        if(!Objects.equals(requestDto.getTitle(), task.getTitle())){
            task.setTitle(requestDto.getTitle());
        }
        if(!Objects.equals(requestDto.getDescription(), task.getDescription())){
            task.setDescription(requestDto.getDescription());
        }
        if(requestDto.getTaskPriority() != task.getTaskPriority()){
            task.setTaskPriority(requestDto.getTaskPriority());
        }
        if(requestDto.getTaskStatus() != task.getTaskStatus()){
            if(LocalDateTime.now().isBefore(requestDto.getDueDate()) && requestDto.getTaskStatus()==TaskStatusType.OVERDUE){
                //pass
            }
            else{
                task.setTaskStatus(requestDto.getTaskStatus());
            }
        }
        if(requestDto.getDueDate() != task.getDueDate()){
            task.setDueDate(requestDto.getDueDate());
        }
        tasksRepository.save(task);
        new TaskDto(
                task.getTaskId(),
                task.getTitle(),
                task.getDescription(),
                task.getTaskPriority(),
                task.getTaskStatus(),
                task.getDueDate(),
                task.getUser().getUserId()
        );
    }

    @Override
    public void deleteTask(Long id) {
        if(!tasksRepository.existsById(id)){
            throw new EntityNotFoundException("Task with id " + id + " doesn't exist");
        }
        Tasks task= tasksRepository.findById(id).orElseThrow();
        task.setTaskStatus(TaskStatusType.DELETED);
        tasksRepository.save(task);
    }

    @Override
    public TaskDto fetchTask(Long id) {
        Tasks existingTask = tasksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        return TaskDto.builder()
                .taskId(existingTask.getTaskId())
                .title(existingTask.getTitle())
                .description(existingTask.getDescription())
                .taskPriority(existingTask.getTaskPriority())
                .taskStatus(existingTask.getTaskStatus())
                .taskId(existingTask.getTaskId())
                .dueDate(existingTask.getDueDate())
                .userId(existingTask.getUser().getUserId())
                .build();
    }

    @Override
    public List<TaskDto> getAllTasksAdmin() {
        return tasksRepository.getAllTasks().stream().map(
                task -> new TaskDto(
                        task.getTaskId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getTaskPriority(),
                        task.getTaskStatus(),
                        task.getDueDate(),
                        task.getUser().getUserId())
                ).toList();
    }


}
