package com.soundboard.soundboard.unit.service;

import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.responseModels.user.LoginResponse;
import com.soundboard.soundboard.models.responseModels.user.RegisterResponse;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.security.JWTService;
import com.soundboard.soundboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.soundboard.soundboard.util.Constants.BCRYPT_STRENGTH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestUserService {
  
  @Mock
  MyUserRepo userRepo;
  
  @Mock
  AuthenticationManager authenticationManager;
  
  @Mock
  JWTService jwtService;
  
  private UserService userService;
  
  @BeforeEach
  void setUp() {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    userService = new UserService(userRepo, authenticationManager, jwtService, passwordEncoder);
  }
  
  @Test
  void testRegisterUser_Success() {
    Users user = Users.builder()
            .id(1L)
            .username("TestUser")
            .password("password")
            .build();
  
    RegisterResponse expected = RegisterResponse.builder()
            .username("TestUser")
            .message("User registered successfully")
            .build();
    
    RegisterResponse actual = userService.registerUser(user);
    
    assert actual != null;
    assert actual.username().equals(expected.username());
    assert actual.message().equals(expected.message());
    
  }
  
  @Test
  void testVerfiy_Success() {
    Users user = Users.builder()
            .id(1L)
            .username("TestUser")
            .password("password")
            .build();
    
    Authentication authentication = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(jwtService.generateToken("TestUser")).thenReturn("mockedToken");
  
    LoginResponse expected = LoginResponse.builder()
            .username("TestUser")
            .token("mockedToken")
            .message("User authenticated successfully")
            .build();
    
    LoginResponse actual = userService.verify(user);
    
    assert actual != null;
    assert actual.username().equals(expected.username());
    assert actual.token().equals(expected.token());
    assert actual.message().equals(expected.message());
  }
  
  @Test
  void testVerify_Failure() {
    Users user = Users.builder()
            .id(1L)
            .username("TestUser")
            .password("wrongPassword")
            .build();
    
    Authentication authentication = mock(Authentication.class);
    
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);
    
    LoginResponse expected = LoginResponse.builder()
            .username("TestUser")
            .token("")
            .message("Invalid username or password")
            .build();
    
    LoginResponse actual = userService.verify(user);
    
    assert actual != null;
    assert actual.username().equals(expected.username());
    assert actual.token().equals(expected.token());
    assert actual.message().equals(expected.message());
  }
  
}
