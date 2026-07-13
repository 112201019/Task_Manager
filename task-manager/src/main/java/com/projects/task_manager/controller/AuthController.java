package com.projects.task_manager.controller;

import com.projects.task_manager.dto.AuthRequestDto;
import com.projects.task_manager.security.JwtUtility;
import com.projects.task_manager.service.implementations.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final LoginAttemptService loginAttemptService;
    private final JwtUtility jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequestDto request) {
        String identifier = request.getLoginIdentifier();

        // 1. Check Redis BEFORE attempting authentication
        if (loginAttemptService.isBlocked(identifier)) {
            log.warn("Login blocked by Redis cache for: {}", identifier);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Account locked due to multiple failed attempts. Try again in 15 minutes."));
        }

        try {
            // 2. Attempt Authentication
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword());

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 3. Success! Clear their record from Redis
            loginAttemptService.loginSucceeded(identifier);

            // Generate Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // 4. Failure! Tell Redis to increment the counter and restart the 15-minute timer
            loginAttemptService.loginFailed(identifier);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }
}