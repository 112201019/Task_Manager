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

    public void loginFailed(String identifier) {
        String key = "login_attempts:" + identifier;

        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = (attemptsStr == null) ? 1 : Integer.parseInt(attemptsStr) + 1;

        redisTemplate.opsForValue().set(key, String.valueOf(attempts), Duration.ofMinutes(LOCK_TIME_DURATION_MINUTES));
    }

    public void loginSucceeded(String identifier) {
        String key = "login_attempts:" + identifier;
        redisTemplate.delete(key);
    }

    public boolean isBlocked(String identifier) {
        String key = "login_attempts:" + identifier;
        String attemptsStr = redisTemplate.opsForValue().get(key);

        if (attemptsStr != null) {
            return Integer.parseInt(attemptsStr) >= MAX_ATTEMPTS;
        }
        return false;
    }
}