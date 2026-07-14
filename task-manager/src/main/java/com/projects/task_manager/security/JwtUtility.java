package com.projects.task_manager.security;

import com.projects.task_manager.entity.Users;
import com.projects.task_manager.service.implementations.UsersService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtility {

    private final UsersService usersService;
    private final SecretKey key;
    public JwtUtility(UsersService usersService, @Value("${jwt.secretKey}") String secretKey){
         this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
         this.usersService = usersService;

    }

    public String generateToken(UserDetails userDetails) {
        Users user = (Users) userDetails;
        return Jwts.builder()
                .claim("username", user.getDisplayName())
                .subject(user.getEmail())
                .issuer("task-manager-api")
                .audience().add("task-manager-ui").and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // Set to 15 mins
                .signWith(key)
                .compact();
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String tokenEmail = extractUsername(token);
        Users user = (Users) userDetails;

        boolean isExpired = isTokenExpired(token);

        return (tokenEmail.equals(user.getEmail())) && !isExpired;
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer("task-manager-api")
                .requireAudience("task-manager-ui")
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claimsResolver.apply(claims);
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
