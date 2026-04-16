package com.soundboard.soundboard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
  
  private final String secretKey; // currently this key is generated every time the
  // application is restarted, so all existing tokens will be invalidated.
  // in a real application, you would likely want to store this key in a secure
  // location and load it when the application starts up.
  // you could store this key in an environment variable, a configuration file, or a secure vault.
  // to get from the environment variable you would use System.getenv("SECRET_KEY") and to get from
  // a configuration file you would use @Value("${secret.key}") and ensure that the key is set in
  // the application.properties file.
  // you could reference the key in the application.properties file like this: secret.key=your_secret_key_here
  // you can store the key in .env and reference it in the application.properties file like this: secret.key=${SECRET_KEY}
  
  public JWTService() {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256"); // instantiate the keyGenerator
      SecretKey sk = keyGenerator.generateKey(); // create the secret key
      secretKey = Base64.getEncoder().encodeToString(sk.getEncoded()); //Base64 encode the SecretKey to String and store in instance variable
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
  
  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    
    return Jwts.builder()
            .claims()
              .add(claims)
              .subject(username)
              .issuedAt(new Date(System.currentTimeMillis()))
              .expiration(new Date(System.currentTimeMillis() + 60*60*30))
            .and()
            .signWith(getKey()) //generate key here and sign the JWT with it.
            .compact();
  }
  
  private SecretKey getKey() { // method to fetch the SecretKey - decode the instance variable into bytes, return the key
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes); // convert the bytes into a key using the same algorithm type for the KeyGenerator
  }
  
  public String extractUserName(String token) {
    return extractClaim(token, Claims::getSubject);
  }
  
  private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    final Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }
  
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }
  
  public Boolean validateToken(String token, UserDetails userDetails) {
    final String userName = extractUserName(token);
    return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
  
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }
  
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }
}
