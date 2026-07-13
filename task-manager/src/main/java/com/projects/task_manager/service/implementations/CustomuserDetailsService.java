package com.projects.task_manager.service.implementations;

import com.projects.task_manager.entity.Users;
import com.projects.task_manager.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomuserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String loginIdentifier) throws UsernameNotFoundException {
        return usersRepository.findByEmailOrUsername(loginIdentifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID/Email: " + loginIdentifier));
    }
}
