package com.soundboard.soundboard.service;

import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.responseModels.user.LoginResponse;
import com.soundboard.soundboard.models.responseModels.user.RegisterResponse;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.security.JWTService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  
  private MyUserRepo userRepo;
  AuthenticationManager authenticationManager;
  JWTService jwtService;
  PasswordEncoder passwordEncoder;
  
  public UserService(MyUserRepo userRepo,
                     AuthenticationManager authenticationManager,
                     JWTService jwtService,
                     PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  
  }
  
  public RegisterResponse registerUser(Users user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepo.save(user);
    return RegisterResponse.builder()
            .username(user.getUsername())
            .message("User registered successfully")
            .build();
  }
  
  public LoginResponse verify(Users user) {
    Authentication authentication =
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
    if (authentication.isAuthenticated()) return LoginResponse.builder()
            .username(user.getUsername())
            .token(jwtService.generateToken(user.getUsername()))
            .message("User authenticated successfully")
            .build();
    
    return LoginResponse.builder()
            .username(user.getUsername())
            .token("")
            .message("Invalid username or password")
            .build();
    
  }
}
