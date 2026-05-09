package com.soundboard.soundboard.security.filter;

import com.soundboard.soundboard.security.JWTService;
import com.soundboard.soundboard.service.MyUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter { // filter executed only once per request.
  
  @Autowired
  JWTService jwtService;
  
  @Autowired
  MyUserDetailsService userDetailsService;
  
  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
          throws ServletException, IOException {
    
    String authHeader = request.getHeader("Authorization");
    String token = null;
    String username = null;
    
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
      try {
        username = jwtService.extractUserName(token);
      } catch (JwtException e) {
        filterChain.doFilter(request, response);
        return;
      }
    }
  
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      try {
        new AccountStatusUserDetailsChecker().check(userDetails);
      } catch (AccountStatusException e) {
        filterChain.doFilter(request, response);
        return;
      }

      if (jwtService.validateToken(token, userDetails)) {
        String role = jwtService.extractRole(token);
        List<GrantedAuthority> authorities = (role != null)
                ? List.of(new SimpleGrantedAuthority(role))
                : List.copyOf(userDetails.getAuthorities());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource()
                .buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    filterChain.doFilter(request, response);
    
  }
}
