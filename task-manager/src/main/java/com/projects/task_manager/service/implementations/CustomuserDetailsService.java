package com.projects.task_manager.service.implementations;

import com.projects.task_manager.entity.Users;
import com.projects.task_manager.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.projects.task_manager.service.implementations.isNumber.isNumeric;

@Service
@RequiredArgsConstructor
public class CustomuserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;
    @Override
    public UserDetails loadUserByUsername(@NonNull String loginIdentity) throws UsernameNotFoundException {
        Users user = null;
        if(isNumeric(loginIdentity))
            user = usersRepository.findById((long) Double.parseDouble(loginIdentity)).orElseThrow();
        else{
            user = usersRepository.findByEmail(loginIdentity).orElseThrow();
        }
        return user;

    }
}
