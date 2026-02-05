package com.soundboard.soundboard.service;

import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.soundboard.soundboard.util.Constants.BCRYPT_STRENGTH;

@Service
public class UserService {
  @Autowired
  private MyUserRepo userRepo;
  
  @Autowired
  AuthenticationManager authenticationManager;
  
  @Autowired
  JWTService jwtService;
  
  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
  
  public Users registerUser(Users user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepo.save(user);
  }
  
  public String verify(Users user) {
    Authentication authentication =
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
    if (authentication.isAuthenticated()) return jwtService.generateToken(user.getUsername());
    
    return "Fail";
    
  }
}
