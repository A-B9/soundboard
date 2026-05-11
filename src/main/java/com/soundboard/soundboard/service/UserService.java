package com.soundboard.soundboard.service;

import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.requestModels.LoginRequest;
import com.soundboard.soundboard.models.requestModels.RegisterRequest;
import com.soundboard.soundboard.models.responseModels.user.LoginResponse;
import com.soundboard.soundboard.models.responseModels.user.RegisterResponse;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.security.JWTService;
import com.soundboard.soundboard.security.MyUserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {
  
  private final MyUserRepo userRepo;
  private final AuthenticationManager authenticationManager;
  private final JWTService jwtService;
  private final PasswordEncoder passwordEncoder;
  
  public UserService(MyUserRepo userRepo,
                     AuthenticationManager authenticationManager,
                     JWTService jwtService,
                     PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  
  }
  
  public RegisterResponse registerUser(RegisterRequest request) {
    if (userRepo.existsByUsername(request.username())) {
      return RegisterResponse.builder()
              .username(request.username())
              .message("Username already exists")
              .build();
    }
    Users user = Users.builder()
            .username(request.username())
            .active(true)
            .createdAt(Instant.now())
            .build();
    user.setPassword(passwordEncoder.encode(request.password()));
    userRepo.save(user);
    return RegisterResponse.builder()
            .username(request.username())
            .message("User registered successfully")
            .role(Role.USER)
            .build();
  }
  
  public LoginResponse verify(LoginRequest request) {
    try {
      Authentication authentication =
              authenticationManager.authenticate(
                      new UsernamePasswordAuthenticationToken(request.username(), request.password())
              );
      if (authentication.isAuthenticated()) {
        if (!(authentication.getPrincipal() instanceof MyUserPrincipal principal)) {
          return LoginResponse.builder()
                  .username(request.username())
                  .token("")
                  .message("Invalid username or password")
                  .build();
        }
        return LoginResponse.builder()
                .username(request.username())
                .token(jwtService.generateToken(request.username(), principal.getRole(), principal.isMustChangePassword()))
                .message("User authenticated successfully")
                .build();
      }
    } catch (AuthenticationException e) {
      // intentionally vague — does not indicate whether username or password was wrong
    }
    return LoginResponse.builder()
            .username(request.username())
            .token("")
            .message("Invalid username or password")
            .build();
  }
}
