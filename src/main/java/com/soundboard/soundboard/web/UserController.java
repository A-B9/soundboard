package com.soundboard.soundboard.web;

import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
  
  @Autowired
  MyUserDetailsService userService;
  
  @PostMapping("/register")
  public Users registerUser(@RequestBody Users user) {
    return userService.registerUser(user);
  }
  
}
