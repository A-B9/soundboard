package com.soundboard.soundboard.security;

import com.soundboard.soundboard.security.filter.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import static com.soundboard.soundboard.util.Constants.BCRYPT_STRENGTH;

@Configuration
@EnableWebSecurity // don't use default flow - use this flow
public class SecurityConfig {
  
  @Autowired
  private UserDetailsService userDetailsService;
  
  @Autowired
  private JwtFilter jwtFilter;
  
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
  // what is the spring security filter chain? this is a chain of filters that are
    // applied to every request that comes into the application. each filter can modify the request
    // and response objects and decide whether to continue the chain or not. the order of the
    // filters is important because it determines how the request is processed
    // and what security measures are applied.
    
    // what are the default filters in the spring security filter chain? there are many filters that are applied by default, such as:
    // - SecurityContextPersistenceFilter: this filter is responsible for storing the security context in the session and retrieving it for each request.
    // - UsernamePasswordAuthenticationFilter: this filter is responsible for processing the login form and authenticating the user.
    // - BasicAuthenticationFilter: this filter is responsible for processing basic authentication headers and authenticating the user.
    // - CsrfFilter: this filter is responsible for protecting against cross-site request forgery attacks.

    return httpSecurity
            .csrf((csrf) -> csrf.disable()) // protect against csrf attacks.
            .authenticationProvider(authenticationProvider()) // provide custom authentication provider
            .authorizeHttpRequests(
                    request -> request
                            .requestMatchers("/register", "/login").permitAll() // allow unauthenticated access to these endpoints
                            .anyRequest().authenticated() // ensure that all other requests can only be called by authenticated users.
            )
            .redirectToHttps(Customizer.withDefaults()) // redirect to https if request is http. this is important for security because it ensures that all communication between the client and server is encrypted.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // no sessions are ever created or used if provided.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // add this filter before the default username password filter so that it can check for jwt token and authenticate user before the request is processed by the default filter.
            .passwordManagement((passwordManager) -> passwordManager.changePasswordPage("/change-password")) // provide custom change password page
            .logout((logout) ->
                    logout.deleteCookies("SOME_COOKIE")
                            .invalidateHttpSession(true) // not needed since we are stateless but good practice to invalidate session on logout.
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/logouts-success"))
            .build();

  }
  
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
  }
  
  @Bean
  AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }
  
  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
    // is the api that defines how springs security filters work and how authentication is performed.
    // it is used to authenticate a user based on the provided credentials and the configured authentication
    // provider.
    // most common implementation of this interface is the ProviderManager,
    // which delegates authentication to a list of configured authentication providers.
    return configuration.getAuthenticationManager();
  }
  
  @Bean
  AnnotationTemplateExpressionDefaults templateExpressionDefaults() {
    
    return new AnnotationTemplateExpressionDefaults();
  }
  
}
