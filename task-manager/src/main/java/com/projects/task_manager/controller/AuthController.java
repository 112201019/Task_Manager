package com.projects.task_manager.controller;

import com.projects.task_manager.dto.AuthRequestDto;
import com.projects.task_manager.entity.RefreshToken;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.security.JwtUtility;
import com.projects.task_manager.service.implementations.LoginAttemptService;
import com.projects.task_manager.service.implementations.RefreshTokenService;
import com.projects.task_manager.service.implementations.TokenDenylistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final LoginAttemptService loginAttemptService;
    private final JwtUtility jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final TokenDenylistService tokenDenylistService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequestDto request, HttpServletResponse response) {
        String rawIdentifier = request.getLoginIdentifier();

        String cacheIdentifier = rawIdentifier != null ? rawIdentifier.trim().toLowerCase() : "";

        if (loginAttemptService.isBlocked(cacheIdentifier)) {
            log.warn("Login blocked by Redis cache for: {}", cacheIdentifier);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Account locked due to multiple failed attempts. Try again in 15 minutes."));
        }

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(rawIdentifier, request.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            loginAttemptService.loginSucceeded(cacheIdentifier);

            Users user = (Users) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(user);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());
            // Build the secure Spring ResponseCookie
            ResponseCookie springCookie = ResponseCookie.from("refresh_token", refreshToken.getToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/api/auth")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();
            Map<String, String> resBody = new HashMap<>();
            resBody.put("token", jwt);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                    .body(resBody);

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(cacheIdentifier);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenString = getRefreshTokenFromCookies(request);
        if (refreshTokenString == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Refresh token missing. Please log in."));
        }

        Optional<RefreshToken> tokenOpt = refreshTokenService.findByToken(refreshTokenString);
        if (tokenOpt.isPresent()) {
            RefreshToken oldToken = tokenOpt.get();
            try {
                refreshTokenService.verifyExpiration(oldToken);
                Users user = oldToken.getUser();

                // 1. ROTATION: Destroy the old refresh token so it cannot be reused
                refreshTokenService.deleteToken(oldToken);

                // 2. Create a brand new refresh token
                RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUserId());

                // 3. Build the secure Spring ResponseCookie
                ResponseCookie springCookie = ResponseCookie.from("refresh_token", newRefreshToken.getToken())
                        .httpOnly(true)
                        .secure(false) // Keep false for localhost testing
                        .path("/api/auth")
                        .maxAge(7 * 24 * 60 * 60)
                        .sameSite("Strict")
                        .build();
                String newAccessToken = jwtUtil.generateToken(user);

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                        .body(Map.of("token", newAccessToken));

            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid refresh token."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenString = getRefreshTokenFromCookies(request);
        if (refreshTokenString != null) {
            refreshTokenService.findByToken(refreshTokenString).ifPresent(token -> {
                refreshTokenService.deleteByUserId(token.getUser().getUserId());
            });
        }

        // Build a secure wipe cookie
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth") // Must explicitly match the creation path to delete properly
                .maxAge(0)
                .sameSite("Strict")
                .build();

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                java.util.Date expiration = jwtUtil.extractExpiration(jwt);
                long timeToLive = expiration.getTime() - System.currentTimeMillis();

                if (timeToLive > 0) {
                    tokenDenylistService.addToDenylist(jwt, timeToLive);
                }
            } catch (Exception e) {
                log.warn("Attempted to logout with an already expired or invalid token.");
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}