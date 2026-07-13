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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime; // Make sure to add this import at the top
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TasksService implements TasksServiceInterface{

    private final UsersRepository UsersRepository;
    private final TasksRepository tasksRepository;

    //create new tasks
    @Transactional
    public void createNewTask(TaskRequestDto request, UUID userId){
        Users user = UsersRepository.findById(userId) // UPDATED
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        LocalDateTime finalDueDate = request.getDueDate();
        if (request.isRecurring() && finalDueDate == null) {
            finalDueDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        }

        Tasks newTask = Tasks.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .taskPriority(request.getTaskPriority())
                .taskStatus(request.getTaskStatus())
                .isRecurring(request.isRecurring())
                .dueDate(finalDueDate)
                .build();

        newTask.setUser(user);
        user.getTasks().add(newTask);
        tasksRepository.save(newTask);
    }

    @Override
    public List<TaskDto> getAllTasks(UUID userId) {
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
                        k.isRecurring(),
                        k.getDueDate(),
                        k.getUser().getUserId())
                ).toList();
    }

    @Override
    public void updateTask(UUID id, TaskRequestDto requestDto) {
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
            if(requestDto.getDueDate() != null && LocalDateTime.now().isBefore(requestDto.getDueDate()) && requestDto.getTaskStatus()==TaskStatusType.OVERDUE){
                //pass
            }
            else{
                task.setTaskStatus(requestDto.getTaskStatus());
            }
        }
        boolean isMarkedDone = (requestDto.getTaskStatus() == TaskStatusType.DONE);
        task.setTaskStatus(requestDto.getTaskStatus());

        if (isMarkedDone && task.isRecurring()) {
            // Push the date 1 day front
            LocalDateTime nextDueDate = task.getDueDate() != null
                    ? task.getDueDate().plusDays(1)
                    : LocalDateTime.now().plusDays(1).withHour(23).withMinute(59);

            Tasks clonedTask = Tasks.builder()
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .taskPriority(task.getTaskPriority())
                    .taskStatus(TaskStatusType.TO_DO)
                    .isRecurring(true)
                    .dueDate(nextDueDate)
                    .user(task.getUser())
                    .build();
            tasksRepository.save(clonedTask);
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
                task.isRecurring(),
                task.getDueDate(),
                task.getUser().getUserId()
        );
    }

    @Override
    public void deleteTask(UUID id) {
        if(!tasksRepository.existsById(id)){
            throw new EntityNotFoundException("Task with id " + id + " doesn't exist");
        }
        Tasks task= tasksRepository.findById(id).orElseThrow();
        task.setTaskStatus(TaskStatusType.DELETED);
        tasksRepository.save(task);
    }

    @Override
    public TaskDto fetchTask(UUID id) {
        Tasks existingTask = tasksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + id));
        return TaskDto.builder()
                .taskId(existingTask.getTaskId())
                .title(existingTask.getTitle())
                .description(existingTask.getDescription())
                .taskPriority(existingTask.getTaskPriority())
                .taskStatus(existingTask.getTaskStatus())
                .isRecurring(existingTask.isRecurring())
                .dueDate(existingTask.getDueDate())
                .userId(existingTask.getUser().getUserId())
                .build();
    }

    @Override
    public Page<TaskDto> getAllTasksAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tasksRepository.getAllTasks(pageable)
                .map(task -> new TaskDto(
                        task.getTaskId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getTaskPriority(),
                        task.getTaskStatus(),
                        task.isRecurring(),
                        task.getDueDate(),
                        task.getUser().getUserId()
                ));
    }


}
