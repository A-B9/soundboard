package com.soundboard.soundboard.security;

import com.soundboard.soundboard.models.Users;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class MyUserPrincipal implements UserDetails {
  
  private final transient Users user;
  
  public MyUserPrincipal(Users user) {
    this.user = user;
  }
  
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    //what are authorities - they are the roles or permissions that a user has.
    // they are used to determine what actions a user can perform in the application.
    // in this case, we are assuming that all users have the same authority of "USER".
    // in a real application, you would likely have different authorities for different
    // users, and you would retrieve them from the database.
    
    // when would the authorities for a user typically be set
    // - when the user is created or updated in the database.
    // you would likely have a field in the database that indicates the user's role or
    // permissions and you would set the authorities based on that field.
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
    // how would you implement this in a real application? you would likely have a field in the database that
    // indicates whether the account is expired or not and you would check that field here.
    // could this be checked via jwt token - yes if the token is invalid or expired then the user
    // would not be authenticated and therefore would not be able to access any protected resources.
    return true;
  }
  
  @Override
  public boolean isAccountNonLocked() {
    //assume account is not locked
    // how would you implement this in a real application? you would likely
    // have a field in the database that indicates whether the account is locked
    // or not and you would check that field here.
    // could this be checked via jwt token - yes if the token is invalid or expired then the
    // user would not be authenticated and therefore would not be able to access any protected resources.
    return true;
  }
  
  @Override
  public boolean isCredentialsNonExpired() {
    //assume credentials are not expired
    // what are credentials - they are the password or other authentication
    // information that a user provides to authenticate themselves.
    return true;
  }
  
  @Override
  public boolean isEnabled() {
    // assume account is enabled
    return true;
  }
}
