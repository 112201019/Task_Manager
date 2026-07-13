package com.projects.task_manager.service.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION_MINUTES = 15;

    // 1. Increment the failed attempts
    public void loginFailed(String identifier) {
        String key = "login_attempts:" + identifier;

        // Get current attempts, defaulting to 0
        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = (attemptsStr == null) ? 1 : Integer.parseInt(attemptsStr) + 1;

        // Save back to Redis and strictly set the Time-To-Live (TTL) to 15 minutes
        redisTemplate.opsForValue().set(key, String.valueOf(attempts), Duration.ofMinutes(LOCK_TIME_DURATION_MINUTES));
    }

    // 2. Clear the cache on a successful login
    public void loginSucceeded(String identifier) {
        String key = "login_attempts:" + identifier;
        redisTemplate.delete(key);
    }

    // 3. Check if the user is currently locked out
    public boolean isBlocked(String identifier) {
        String key = "login_attempts:" + identifier;
        String attemptsStr = redisTemplate.opsForValue().get(key);

        if (attemptsStr != null) {
            return Integer.parseInt(attemptsStr) >= MAX_ATTEMPTS;
        }
        return false;
    }
}