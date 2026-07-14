package com.projects.task_manager.service.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TokenDenylistService {

    private final StringRedisTemplate redisTemplate;

    // Helper method to hash the token
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    public void addToDenylist(String token, long timeToLiveMillis) {
        // Store the HASH, not the raw token
        String key = "jwt_denylist:" + hashToken(token);
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(timeToLiveMillis));
    }

    public boolean isDenied(String token) {
        // Check the HASH, not the raw token
        String key = "jwt_denylist:" + hashToken(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}