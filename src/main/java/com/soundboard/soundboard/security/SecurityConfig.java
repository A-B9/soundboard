package com.soundboard.soundboard.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static com.soundboard.soundboard.util.Constants.BCRYPT_STRENGTH;

@Configuration
@EnableWebSecurity // don't use default flow - use this flow
public class SecurityConfig {
  
  @Autowired
  private UserDetailsService userDetailsService;
  
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {

    return httpSecurity
            .csrf(customizer -> customizer.disable()) // remove csrf
            .authorizeHttpRequests(
                    request -> request.anyRequest().authenticated() // ensure that all requests can only be called by authenticated users.
            )
            .httpBasic(Customizer.withDefaults()) //configures basic authentication
            
            .sessionManagement(
                    session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no sessions are ever created or used if provided.
            ).build();

  }
  
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
  }
  
  public String encodePassword(String password) {
    return passwordEncoder().encode(password);
  }
  
  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }
  
}
