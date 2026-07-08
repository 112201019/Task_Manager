package com.projects.task_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddUserRequestDto {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 30, message = "Name should be of length 3 to 100 characters")
    private String username;

    @Email
    @NotBlank(message = "Email is required")
    @Size(min = 3, max = 30, message = "email should be of length 3 to 100 characters")
    private String email;

    @NotBlank(message = "Create a password")
    private String password;

    private String adminCode;

}
