package com.projects.task_manager.service.implementations;

import com.projects.task_manager.dto.AddUserRequestDto;
import com.projects.task_manager.dto.ChangePasswordDto;
import com.projects.task_manager.dto.EditUserDto;
import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.entity.type.Role;
import com.projects.task_manager.repository.TasksRepository;
import com.projects.task_manager.repository.UsersRepository;
import com.projects.task_manager.service.UserServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersService implements UserServiceInterface {

    private final UsersRepository UsersRepository;
    private final TasksRepository TasksRepository;
    private final PasswordEncoder passwordEncoder;


    public UserDto fetchUser(UUID id) {
        Users user = UsersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + id));
        return new UserDto(user.getUserId(),user.getDisplayName(),user.getEmail());
    }

    @Override
    public UserDto createUser(AddUserRequestDto addUserRequestDto) {
        if (UsersRepository.existsByUsername(addUserRequestDto.getUsername())) {
            throw new IllegalArgumentException("That username is already taken!");
        }
        if (UsersRepository.existsByEmail(addUserRequestDto.getEmail())) {
            throw new IllegalArgumentException("An account with that email already exists!");
        }
        Users newUser = Users.builder()
                .username(addUserRequestDto.getUsername())
                .email(addUserRequestDto.getEmail())
                .password(passwordEncoder.encode(addUserRequestDto.getPassword()))
                .role(Role.USER)
                .build();

        Users nUser = UsersRepository.save(newUser);
        return new UserDto(nUser.getUserId(), nUser.getDisplayName(), nUser.getEmail());
    }

    @Override
    public void editUser(EditUserDto ud){
        Users user = UsersRepository.findById(ud.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UsersRepository.findByEmailOrUsername(ud.getUsername())
                .ifPresent(existingUser -> {
                    if (!existingUser.getUserId().equals(ud.getUserId()) && existingUser.getUsername().equals(ud.getUsername())) {
                        throw new IllegalArgumentException("Username is already in use.");
                    }
                });

        UsersRepository.findByEmail(ud.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getUserId().equals(ud.getUserId())) {
                        throw new IllegalArgumentException("Email is already in use.");
                    }
                });

        user.setEmail(ud.getEmail());
        user.setUsername(ud.getUsername());
        UsersRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!UsersRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        UsersRepository.deleteById(id);
    }
    @Override
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return UsersRepository.findAll(pageable)
                .map(user -> new UserDto(
                        user.getUserId(),
                        user.getDisplayName(),
                        user.getEmail()
                ));
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordDto request) {
        Users user = UsersRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        UsersRepository.save(user);
    }

//now handled by authcontroller
    //    public Boolean authenticateUser(String Id, String password){
//        Users user = null;
//        if(isNumeric(Id)) {
//            user = UsersRepository.findById((long) Double.parseDouble(Id)).orElseThrow();
//        }
//        else{
//            user = UsersRepository.findByEmail(Id).orElseThrow();
//        }
//        return (Objects.equals(user.getPassword(), password));

//    }
}
