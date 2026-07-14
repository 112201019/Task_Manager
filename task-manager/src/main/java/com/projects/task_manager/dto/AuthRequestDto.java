package com.projects.task_manager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDto {
    @NotBlank
    private String loginIdentifier;
    @NotBlank
    private String password;
}