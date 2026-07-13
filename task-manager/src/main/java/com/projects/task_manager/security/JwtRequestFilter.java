package com.projects.task_manager.security;

import com.projects.task_manager.service.implementations.CustomuserDetailsService;
import com.projects.task_manager.service.implementations.TokenDenylistService; // NEW
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomuserDetailsService userDetailsService;
    private final JwtUtility jwtUtil;
    private final TokenDenylistService tokenDenylistService; // NEW INJECTION

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            // NEW: Instantly reject if the token is in the Redis denylist
            if (tokenDenylistService.isDenied(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Token has been revoked. Please log in again.\"}");
                response.setContentType("application/json");
                return; // Stop the filter chain immediately
            }

            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception _) {

            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken uPassToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                uPassToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(uPassToken);
            }
        }

        chain.doFilter(request, response);
    }
}