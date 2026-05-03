package com.soundboard.soundboard.security;

import com.soundboard.soundboard.security.filter.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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

  @Value("${security.require-https:true}")
  private boolean requireHttps;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
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

    httpSecurity
            .csrf((csrf) -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(
                    request -> request
                            .requestMatchers("/register", "/login").permitAll()
                            .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .passwordManagement((passwordManager) -> passwordManager.changePasswordPage("/change-password"))
            .logout((logout) ->
                    logout.deleteCookies("SOME_COOKIE")
                            .invalidateHttpSession(true)
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/logouts-success"));

    if (requireHttps) {
      httpSecurity.redirectToHttps(Customizer.withDefaults());
    }

    return httpSecurity.build();

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
