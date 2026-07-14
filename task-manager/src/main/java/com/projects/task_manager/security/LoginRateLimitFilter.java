package com.projects.task_manager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    // Injecting the Redis-backed Proxy Manager we just created
    private final ProxyManager<byte[]> proxyManager;

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filter) throws ServletException, IOException {
        if (req.getRequestURI().equals("/api/auth/login") && req.getMethod().equalsIgnoreCase("POST")) {

            String ipAddress = getClientIP(req);
            String key = "ip_rate_limit:" + ipAddress;

            BucketConfiguration configuration = BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder().capacity(50).refillIntervally(50, Duration.ofMinutes(15)).build())
                    .build();

            Bucket bucket = proxyManager.builder().build(key.getBytes(), configuration);

            if (!bucket.tryConsume(1)) {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Too many login attempts from this IP. Please try again later.\"}");
                return; // Block the request here
            }
        }

        filter.doFilter(req, res);
    }
}