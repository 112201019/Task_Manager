package com.projects.task_manager.controller;

import com.projects.task_manager.dto.AuthRequestDto;
import com.projects.task_manager.security.JwtUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    private final JwtUtility jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequestDto request) {
        log.info("Entered /login controller in authcontroller");
        // Pass the generic login identifier
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getLoginIdentifier(), request.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        log.info("Exiting /login controller in authcontroller");
        return ResponseEntity.ok(response);
    }
}