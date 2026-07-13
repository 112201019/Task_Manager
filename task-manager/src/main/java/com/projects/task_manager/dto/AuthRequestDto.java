package com.projects.task_manager.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String loginIdentifier;
    private String password;
}