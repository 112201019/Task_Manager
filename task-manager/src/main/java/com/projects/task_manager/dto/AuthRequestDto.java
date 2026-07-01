package com.projects.task_manager.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String loginIdentifier; // Accepts either email or username
    private String password;
}