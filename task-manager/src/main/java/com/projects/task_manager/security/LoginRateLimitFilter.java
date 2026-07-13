package com.projects.task_manager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> loginBucket=new ConcurrentHashMap<>();

    private Bucket createNewBucket(String s){
        Bandwidth lim= Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(15)).build();
        return Bucket.builder().addLimit(lim).build();
    }
    private Bucket getBucket(String ip){
        return loginBucket.computeIfAbsent(ip, this::createNewBucket);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filter) throws ServletException, IOException {
        if(req.getRequestURI().equals("/api/auth/login") && req.getMethod().equalsIgnoreCase("POST")){
            String ipAddress = req.getRemoteAddr();
            Bucket bucket = getBucket(ipAddress);
            if (bucket.tryConsume(1)) {
                filter.doFilter(req, res);
            } else {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Too many login attempts. Please try again later.\"}");
            }
        }
        else{
            filter.doFilter(req, res);
        }
    }

}
