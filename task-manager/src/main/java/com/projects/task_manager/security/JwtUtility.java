package com.projects.task_manager.security;

import com.projects.task_manager.entity.Users;
import com.projects.task_manager.service.implementations.UsersService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtility {

    private final UsersService usersService;

    private final String SECRETKEY= "***REMOVED_JWT***"; //should put in application.properties since it is a secret key

    private final SecretKey key = Keys.hmacShaKeyFor(SECRETKEY.getBytes());

    public String generateToken(UserDetails userDetails) {
        Users user = (Users) userDetails;
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) //10-hours
                .signWith(key)
                .compact();
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String tokenUserId = extractUsername(token);
        Users user = (Users) userDetails;

        boolean isExpired = isTokenExpired(token);

        return (tokenUserId.equals(String.valueOf(user.getUserId()))) && !isExpired;
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claimsResolver.apply(claims);
    }
}
