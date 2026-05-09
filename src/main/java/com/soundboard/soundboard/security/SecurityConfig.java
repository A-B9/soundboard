package com.soundboard.soundboard.security;

import com.soundboard.soundboard.security.filter.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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

  @Value("${app.cors.allowed-origins:http://localhost:3000}")
  private List<String> allowedOrigins;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .csrf((csrf) -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(
                    request -> request
                            .requestMatchers("/register", "/login").permitAll()
                            .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(h -> h
                    .contentTypeOptions(Customizer.withDefaults())
                    .frameOptions(f -> f.sameOrigin())
                    .httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000))
                    .cacheControl(Customizer.withDefaults())
                    .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
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
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
    return configuration.getAuthenticationManager();
  }

  @Bean
  AnnotationTemplateExpressionDefaults templateExpressionDefaults() {
    return new AnnotationTemplateExpressionDefaults();
  }

}
