package com.soundboard.soundboard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {

  private static final String ROLE_CLAIM = "role";
  private static final String ISSUER = "soundboard";
  private static final String DEV_SECRET = "c2VjcmV0LWtleS1mb3ItZGV2LW9ubHktZG8tbm90LXVzZS1pbi1wcm9kdWN0aW9u";

  private final String secretKey;

  @Autowired
  private Environment env;

  public JWTService(@Value("${app.jwt.secret}") String secretKey) {
      this.secretKey = secretKey;
  }

  @PostConstruct
  void validateSecret() {
    if (Arrays.asList(env.getActiveProfiles()).contains("prod") && DEV_SECRET.equals(secretKey)) {
      throw new IllegalStateException("JWT_SECRET must not be the dev default in production");
    }
  }

  public String generateToken(String username) {
    return Jwts.builder()
            .claims()
              .add(ROLE_CLAIM, "ROLE_USER")
              .subject(username)
              .issuer(ISSUER)
              .issuedAt(new Date(System.currentTimeMillis()))
              .expiration(new Date(System.currentTimeMillis() + 2L * 60 * 60 * 1000))
            .and()
            .signWith(getKey())
            .compact();
  }

  private SecretKey getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String extractUserName(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get(ROLE_CLAIM, String.class));
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    final Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getKey())
            .requireIssuer(ISSUER)
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    final Claims claims = extractAllClaims(token);
    final String userName = claims.getSubject();
    return userName.equals(userDetails.getUsername()) && !claims.getExpiration().before(new Date());
  }
}
