package com.soundboard.soundboard.web;

import com.soundboard.soundboard.models.requestModels.ChangePasswordRequest;
import com.soundboard.soundboard.models.requestModels.LoginRequest;
import com.soundboard.soundboard.models.requestModels.RegisterRequest;
import com.soundboard.soundboard.models.responseModels.user.ChangePasswordResponse;
import com.soundboard.soundboard.models.responseModels.user.RegisterResponse;
import com.soundboard.soundboard.models.responseModels.user.LoginResponse;
import com.soundboard.soundboard.security.MyUserPrincipal;
import com.soundboard.soundboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soundboard/user")
public class UserController {

  @Autowired
  UserService userService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
    RegisterResponse response = userService.registerUser(request);
    if (response.message().equals("Username already exists")) {
      return ResponseEntity.badRequest().body(response);
    }
    return ResponseEntity.ok(response);
  }
  
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = userService.verify(request);
    if  (response.message().contains("Invalid")) {
      return ResponseEntity.badRequest().body(response);
    }
    return ResponseEntity.ok(response);
  }
  
  @PostMapping("/password-reset")
  public ResponseEntity<ChangePasswordResponse> changePassword(
          @Valid @RequestBody ChangePasswordRequest request,
          @AuthenticationPrincipal MyUserPrincipal caller) {
    return ResponseEntity.ok(userService.changePassword(request, caller));
  }
  
}
