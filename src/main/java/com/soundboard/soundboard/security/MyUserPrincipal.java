package com.soundboard.soundboard.security;

import com.soundboard.soundboard.models.Users;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class MyUserPrincipal implements UserDetails {
  
  private Users user;
  
  public MyUserPrincipal(Users user) {
    this.user = user;
  }
  
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    //these are the roles of the user.
    return Collections.singleton(new SimpleGrantedAuthority("USER"));
  }
  
  @Override
  public @Nullable String getPassword() {
    return user.getPassword();
  }
  
  @Override
  public String getUsername() {
    return user.getUsername();
  }
  
  @Override
  public boolean isAccountNonExpired() {
    //assume account is not expired.
    return true;
//    return UserDetails.super.isAccountNonExpired();
  }
  
  @Override
  public boolean isAccountNonLocked() {
    //assume account is not locked
    return true;
//    return UserDetails.super.isAccountNonLocked();
  }
  
  @Override
  public boolean isCredentialsNonExpired() {
    //assume credentials are not expired
    return true;
//    return UserDetails.super.isCredentialsNonExpired();
  }
  
  @Override
  public boolean isEnabled() {
    // assume account is enabled
    return true;
//    return UserDetails.super.isEnabled();
  }
}
