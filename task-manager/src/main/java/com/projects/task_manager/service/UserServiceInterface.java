package com.projects.task_manager.service;

import com.projects.task_manager.dto.AddUserRequestDto;
import com.projects.task_manager.dto.EditUserDto;
import com.projects.task_manager.dto.UserDto;

public interface UserServiceInterface {

    UserDto createUser(AddUserRequestDto addUserRequestDto);

    void editUser(EditUserDto ud);

    void deleteUser(Long id);

//    Boolean authenticateUser(String Id, String password); This is handled by spring security

    UserDto fetchUser(String number);

}
