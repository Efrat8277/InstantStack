package com.example.instantstack.service;

import com.example.instantstack.entities.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;
    public String generateToken(String email, AppUser.Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name()); // מוסיפים את התפקיד לתוך הטוקן

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // המזהה הייחודי (האימייל)
                .setIssuedAt(new Date(System.currentTimeMillis())) // מתי נוצר
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // מתי יפוג
                .signWith(SECRET_KEY) // חתימה דיגיטלית
                .compact();
    }

    // חילוץ האימייל מהטוקן
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class); // שולף את ה-Claim שקראנו לו role בזמן יצירת הטוקן
    }

    // בדיקה אם הטוקן פג תוקף
    public boolean isTokenValid(String token) {
        try {
            return !Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            return false; // טוקן מזויף או לא תקין
        }
    }
}
