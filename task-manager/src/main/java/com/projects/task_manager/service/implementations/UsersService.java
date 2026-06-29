package com.projects.task_manager.service.implementations;

import com.projects.task_manager.dto.AddUserRequestDto;
import com.projects.task_manager.dto.EditUserDto;
import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.repository.TasksRepository;
import com.projects.task_manager.repository.UsersRepository;
import com.projects.task_manager.service.UserServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.BcryptPassword4jPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Objects;

import static com.projects.task_manager.service.implementations.isNumber.isNumeric;

@Service
@RequiredArgsConstructor
public class UsersService implements UserServiceInterface {

    private final UsersRepository UsersRepository;
    private final TasksRepository TasksRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto fetchUser(String loginIdentifier) {
        Users user = null;
        if(isNumeric(loginIdentifier)) {
            user = UsersRepository.findById((long) Double.parseDouble(loginIdentifier)).orElseThrow();
        }
        else{
            user = UsersRepository.findByEmail(loginIdentifier).orElseThrow();
        }
        return new UserDto(user.getUserId(),user.getUsername(),user.getEmail());
    }

    @Override
    public UserDto createUser(AddUserRequestDto addUserRequestDto) {
        Users newUser=Users.builder()
                .username(addUserRequestDto.getUsername())
                .email(addUserRequestDto.getEmail())
                .password(passwordEncoder.encode(addUserRequestDto.getPassword()))
                .build();
        Users nUser = UsersRepository.save(newUser);
        return new UserDto(nUser.getUserId(), nUser.getUsername(), nUser.getEmail());
    }

    @Override
    public void editUser(EditUserDto ud){
        Users user = UsersRepository.findById(ud.getUserId()).orElseThrow();
        user.setEmail(ud.getEmail());
        user.setUsername(ud.getUsername());
        UsersRepository.save(user);
    }
    @Override
    public void deleteUser(Long id) {
        if (!UsersRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        UsersRepository.deleteById(id);
    }

    public Boolean authenticateUser(String Id, String password){
        Users user = null;
        if(isNumeric(Id)) {
            user = UsersRepository.findById((long) Double.parseDouble(Id)).orElseThrow();
        }
        else{
            user = UsersRepository.findByEmail(Id).orElseThrow();
        }
        return (Objects.equals(user.getPassword(), password));
    }
}
