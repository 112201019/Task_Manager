package com.projects.task_manager.service.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenDenylistService {

    private final StringRedisTemplate redisTemplate;

    public void addToDenylist(String token, long timeToLiveMillis) {
        String key = "jwt_denylist:" + token;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(timeToLiveMillis));
    }

    public boolean isDenied(String token) {
        String key = "jwt_denylist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}