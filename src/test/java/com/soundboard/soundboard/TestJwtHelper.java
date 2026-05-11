package com.soundboard.soundboard;

import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestJwtHelper {

  @Autowired
  private JWTService jwtService;

  public String generateTokenForUser(String username) {
    return jwtService.generateToken(username, Role.USER, false);
  }

  public String generateTokenForUser(String username, Role role) {
    return jwtService.generateToken(username, role, false);
  }

  public String generateTokenForUser(String username, Role role, boolean mustChangePassword) {
    return jwtService.generateToken(username, role, mustChangePassword);
  }
}
