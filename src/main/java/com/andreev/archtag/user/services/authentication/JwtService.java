package com.andreev.archtag.user.services.authentication;

import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.global.utils.ConfigUtility;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final ConfigUtility configUtility;

    public String extractEmail(String token) {
        return extractClaim(token, "email");
    }

    public String extractFirstName(String token) {
        return extractClaim(token, "firstname");
    }

    public String extractLastName(String token) {
        return extractClaim(token, "lastname");
    }

    public String extractUuid(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public <T> T extractClaim(String token, String claimName) {
        final Claims claims = extractAllClaims(token);
        return (T) claims.get(claimName);
    }

    public String generateToken(UserEntity userEntityDetails) {
        return generateToken(new HashMap<>(), userEntityDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserEntity userEntityDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userEntityDetails.getUuid())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * Integer.parseInt(configUtility.getProperty("authentication.token-expiration"))))
                .claim("email", userEntityDetails.getEmail())
                .claim("firstname", userEntityDetails.getFirstname())
                .claim("lastname", userEntityDetails.getLastname())
                .claim("role", userEntityDetails.getRole().name())
                .claim("isBanned", userEntityDetails.getIsBanned())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        final String uuid = extractUuid(token);
        return uuid != null && !uuid.isEmpty() && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token, UserEntity userEntityDetails) {
        final String uuid = extractUuid(token);
        return uuid.equals(userEntityDetails.getUuid()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        final String SECRET_KEY = configUtility.getProperty("authentication.secret-key");
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
