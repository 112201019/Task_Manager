package com.projects.task_manager.dto;

import com.projects.task_manager.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDto {

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, message = "New password must be at least 6 characters")
    @ValidPassword
    private String newPassword;
}