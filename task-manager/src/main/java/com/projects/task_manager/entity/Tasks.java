package com.projects.task_manager.entity;

import com.projects.task_manager.entity.type.TaskPriority;
import com.projects.task_manager.entity.type.TaskStatusType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false, length = 100)
    private String title;

    private String description;

    @Column(nullable = false)
    private TaskPriority taskPriority;

    @Column(nullable = false)
    private TaskStatusType taskStatus;

    private LocalDateTime dueDate;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private Users user;
}
