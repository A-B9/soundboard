package com.soundboard.soundboard.service;

import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.security.MyUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
  
  @Autowired
  private MyUserRepo userRepo;
  
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Users user = userRepo.findByUsername(username);
    if (user == null) {
      System.out.println("User not found");
      throw new UsernameNotFoundException("User not found");
    }
    return new MyUserPrincipal(user) {
    };
  }
  
  
}
