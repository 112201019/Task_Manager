package com.projects.task_manager.service;

import com.projects.task_manager.dto.AddUserRequestDto;
import com.projects.task_manager.dto.EditUserDto;
import com.projects.task_manager.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserServiceInterface {

    UserDto createUser(AddUserRequestDto addUserRequestDto);

    void editUser(EditUserDto ud);

    void deleteUser(UUID id);

//    Boolean authenticateUser(String Id, String password); This is handled by spring security

    UserDto fetchUser(UUID number);

    Page<UserDto> getAllUsers(int page, int size);
    // Add this to the list of methods
    void changePassword(java.util.UUID userId, com.projects.task_manager.dto.ChangePasswordDto request);
}
