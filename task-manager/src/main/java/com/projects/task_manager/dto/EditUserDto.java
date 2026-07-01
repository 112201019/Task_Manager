package com.projects.task_manager.dto;

import com.projects.task_manager.entity.Tasks;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EditUserDto {
    private UUID userId;
    private String username;
    private String email;
}

