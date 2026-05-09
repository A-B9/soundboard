package com.soundboard.soundboard;

import com.soundboard.soundboard.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestJwtHelper {
  
  @Autowired
  private JWTService jwtService;
  
  public String generateTokenForUser(String username) {
    return jwtService.generateToken(username);
  }
}
