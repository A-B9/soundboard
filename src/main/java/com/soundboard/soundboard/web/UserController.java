package com.soundboard.soundboard.web;

import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.responseModels.user.RegisterResponse;
import com.soundboard.soundboard.models.responseModels.user.LoginResponse;
import com.soundboard.soundboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
  
  @Autowired
  UserService userService;
  
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> registerUser(@RequestBody Users user) {
    if (user.getUsername().isEmpty()) {
      return ResponseEntity.badRequest().body(RegisterResponse.builder()
              .username(user.getUsername())
              .message("Username cannot be empty")
              .build());
    } else if (user.getPassword().isEmpty()) {
      return ResponseEntity.badRequest().body(RegisterResponse.builder()
              .username(user.getUsername())
              .message("Password cannot be empty")
              .build());
    }
  
    RegisterResponse response = userService.registerUser(user);
    if (response.message().equals("Username already exists")) {
      return ResponseEntity.badRequest().body(response);
    }
    
    return ResponseEntity.ok(response);
  }
  
  @PostMapping("/login")
  public LoginResponse login(@RequestBody Users user) {
    return userService.verify(user);
  }
  
}
