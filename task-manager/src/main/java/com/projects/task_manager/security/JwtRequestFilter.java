package com.projects.task_manager.security;

import com.projects.task_manager.service.implementations.CustomuserDetailsService;
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
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomuserDetailsService userDetailsService;
    private final JwtUtility jwtUtil;

    @Override
    //for writing a custom filter we have to often implement a doFilterInternal and add an authentication object in the security context
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization"); //get the authorization header from the request

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); //remove bearer
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("User Valid");
            } catch (Exception e) {
                // Token is expired or tampered with
                System.out.println("Invalid or expired JWT token");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(jwt, userDetails)) { //check token validity if the userDetails matched

                // creating a trusted authenticatiom object
                UsernamePasswordAuthenticationToken uPassToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                uPassToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // By adding that created token into this the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(uPassToken);
            }
        }

        // for continuing the filters process
        chain.doFilter(request, response);
    }
}