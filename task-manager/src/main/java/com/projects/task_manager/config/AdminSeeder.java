package com.projects.task_manager.config;

import com.projects.task_manager.entity.Users;
import com.projects.task_manager.entity.type.Role;
import com.projects.task_manager.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.password:SuperSecretAdmin999!}") // Default fallback if not in properties
    private String adminPassword;

    @Override
    public void run(String @NonNull ... args) throws Exception {
        String adminEmail = "admin@taskmanager.com";

        if (usersRepository.existsByEmail(adminEmail)) {
            log.info("System Admin account already exists. Skipping seeder.");
            return;
        }

        log.info("No System Admin found. Seeding initial admin account...");
        Users adminUser = Users.builder()
                .username("SystemAdmin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        usersRepository.save(adminUser);
        log.info("System Admin account seeded successfully. Email: {}", adminEmail);
    }
}