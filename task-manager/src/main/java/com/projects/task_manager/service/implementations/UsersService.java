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

    @Value("${admin.registration.code}")
    private String secretAdminCode;

    public UserDto fetchUser(UUID id) {
        Users user = UsersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + id));
        return new UserDto(user.getUserId(),user.getDisplayName(),user.getEmail());
    }

    @Override
    public UserDto createUser(AddUserRequestDto addUserRequestDto) {
        // 1. CHECK FOR DUPLICATES FIRST!
        if (UsersRepository.existsByUsername(addUserRequestDto.getUsername())) {
            throw new IllegalArgumentException("That username is already taken!");
        }
        if (UsersRepository.existsByEmail(addUserRequestDto.getEmail())) {
            throw new IllegalArgumentException("An account with that email already exists!");
        }

        // 3. Determine the role based on the adminCode provided in the request
        Role assignedRole = Role.USER;
        if (addUserRequestDto.getAdminCode() != null &&
                addUserRequestDto.getAdminCode().equals(secretAdminCode)) {
            assignedRole = Role.ADMIN;
        }

        // 4. Update the builder to use the dynamically assigned Enum
        Users newUser = Users.builder()
                .username(addUserRequestDto.getUsername())
                .email(addUserRequestDto.getEmail())
                .password(passwordEncoder.encode(addUserRequestDto.getPassword()))
                .role(assignedRole)
                .build();

        Users nUser = UsersRepository.save(newUser);
        return new UserDto(nUser.getUserId(), nUser.getDisplayName(), nUser.getEmail());
    }

    @Override
    public void editUser(EditUserDto ud){
        Users user = UsersRepository.findById(ud.getUserId()).orElseThrow();
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
    public List<UserDto> getAllUsers() {
        return UsersRepository.findAll().stream().map(
                        user -> new UserDto(
                                user.getUserId(),
                                user.getDisplayName(),
                                user.getEmail()))
                .toList();
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
