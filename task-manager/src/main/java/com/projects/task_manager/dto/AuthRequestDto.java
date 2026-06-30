package com.projects.task_manager.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String userId;
    private String password;
}