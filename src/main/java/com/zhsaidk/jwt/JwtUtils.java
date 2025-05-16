package com.zhsaidk.jwt;

import com.zhsaidk.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecretKeyBuilder;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.accessTokenExpiration}")
    private Integer accessTokenExpiration;
    @Value("${jwt.refreshTokenExpiration}")
    private Integer refreshTokenExpiration;

    public SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getKey())
                .compact();

    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getKey())
                .compact();

    }

    public String getUsernameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException exception){
            log.info("Invalid Jwt token: {}", exception.getMessage());
        } catch (ExpiredJwtException exception){
            log.info("Expired JWT Token");
        } catch (UnsupportedJwtException exception){
            log.info("Unsupported JWT exception");
        } catch (IllegalArgumentException exception){
            log.info("JWT claims is empty: {}", exception.getMessage());
        }
        return false;
    }
}
