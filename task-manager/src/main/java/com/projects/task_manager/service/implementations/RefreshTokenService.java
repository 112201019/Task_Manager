package com.projects.task_manager.service.implementations;

import com.projects.task_manager.entity.RefreshToken;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.repository.RefreshTokenRepository;
import com.projects.task_manager.repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsersRepository usersRepository;

    // Setting the refresh token lifespan to 7 days
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    public RefreshToken createRefreshToken(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString()) // Cryptographically secure random string
                .expiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            // If expired, wipe it from the database immediately
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token is expired. Please sign in again.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public int deleteByUserId(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        // Revokes all active sessions for this user
        return refreshTokenRepository.deleteByUser(user);
    }
}