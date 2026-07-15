package com.projects.task_manager.service.implementations;

import com.projects.task_manager.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCleanupService {

    private final UsersRepository usersRepository;

    @Scheduled(cron = "0 0 18 * * TUE")
    @Transactional
    public void executeWeeklyCleanup() {
        log.info("Starting weekly cleanup of expired soft-deleted users...");

        // Calculate the exact timestamp for 7 days ago
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Execute the hard wipe. Thanks to ON DELETE CASCADE in schema.sql,
        // the database will automatically destroy their tasks as well.
        usersRepository.deleteExpiredUsers(sevenDaysAgo);

        log.info("Weekly cleanup complete.");
    }
}