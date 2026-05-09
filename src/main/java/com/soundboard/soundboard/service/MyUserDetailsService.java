package com.soundboard.soundboard.service;

import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.security.MyUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(MyUserDetailsService.class);

  private final MyUserRepo userRepo;

  MyUserDetailsService(MyUserRepo userRepo) {
    this.userRepo = userRepo;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Users user = userRepo.findByUsername(username);
    if (user == null) {
      log.warn("Authentication attempt for unknown user");
      throw new UsernameNotFoundException("User not found");
    }
    return new MyUserPrincipal(user);
  }
}
