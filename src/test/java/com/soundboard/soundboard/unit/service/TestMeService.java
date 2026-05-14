package com.soundboard.soundboard.unit.service;

import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.requestModels.ChangePasswordRequest;
import com.soundboard.soundboard.models.responseModels.user.ChangePasswordResponse;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.security.JWTService;
import com.soundboard.soundboard.security.MyUserPrincipal;
import com.soundboard.soundboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestMeService {

    @Mock private MyUserRepo userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JWTService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepo, authenticationManager, jwtService, passwordEncoder);
    }

    @Test
    void changePassword_updatesPasswordAndClearsFlag() {
        Users user = userWithMustChange("alice", true);
        MyUserPrincipal caller = principal(user);
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "NewSecurePass1!");

        when(userRepo.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("OldPass1!", "hashed-old")).thenReturn(true);
        when(passwordEncoder.matches("NewSecurePass1!", "hashed-old")).thenReturn(false);
        when(passwordEncoder.encode("NewSecurePass1!")).thenReturn("hashed-new");
        when(userRepo.save(user)).thenReturn(user);
        when(jwtService.generateToken("alice", Role.USER, false)).thenReturn("new-token");

        ChangePasswordResponse result = service.changePassword(req, caller);

        assertThat(result.token()).isEqualTo("new-token");
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.getPassword()).isEqualTo("hashed-new");
        verify(userRepo).save(user);
    }

    @Test
    void changePassword_returnsFreshToken_withMustChangePassword_false() {
        Users user = userWithMustChange("bob", true);
        MyUserPrincipal caller = principal(user);
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "NewSecurePass1!");

        when(userRepo.findByUsername("bob")).thenReturn(user);
        when(passwordEncoder.matches("OldPass1!", "hashed-old")).thenReturn(true);
        when(passwordEncoder.matches("NewSecurePass1!", "hashed-old")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-new");
        when(userRepo.save(user)).thenReturn(user);
        when(jwtService.generateToken(eq("bob"), eq(Role.USER), eq(false))).thenReturn("fresh-token");

        ChangePasswordResponse result = service.changePassword(req, caller);

        assertThat(result.token()).isEqualTo("fresh-token");
        verify(jwtService).generateToken("bob", Role.USER, false);
    }

    @Test
    void changePassword_throwsBadRequest_whenCurrentPasswordIncorrect() {
        Users user = userWithMustChange("alice", true);
        MyUserPrincipal caller = principal(user);
        ChangePasswordRequest req = new ChangePasswordRequest("WrongPass!", "NewSecurePass1!");

        when(userRepo.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("WrongPass!", "hashed-old")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(req, caller))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_throwsBadRequest_whenNewPasswordSameAsCurrent() {
        Users user = userWithMustChange("alice", true);
        MyUserPrincipal caller = principal(user);
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "OldPass1!");

        when(userRepo.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("OldPass1!", "hashed-old")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(req, caller))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_worksForUserWithMustChangePassword_false() {
        Users user = userWithMustChange("alice", false);
        MyUserPrincipal caller = principal(user);
        ChangePasswordRequest req = new ChangePasswordRequest("OldPass1!", "NewSecurePass1!");

        when(userRepo.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("OldPass1!", "hashed-old")).thenReturn(true);
        when(passwordEncoder.matches("NewSecurePass1!", "hashed-old")).thenReturn(false);
        when(passwordEncoder.encode("NewSecurePass1!")).thenReturn("hashed-new");
        when(userRepo.save(user)).thenReturn(user);
        when(jwtService.generateToken("alice", Role.USER, false)).thenReturn("token");

        ChangePasswordResponse result = service.changePassword(req, caller);

        assertThat(result.token()).isEqualTo("token");
    }

    // --- Helpers ---

    private Users userWithMustChange(String username, boolean mustChangePassword) {
        Users u = Users.builder()
                .username(username)
                .role(Role.USER)
                .active(true)
                .createdAt(Instant.now())
                .mustChangePassword(mustChangePassword)
                .build();
        u.setPassword("hashed-old");
        return u;
    }

    private MyUserPrincipal principal(Users user) {
        return new MyUserPrincipal(user);
    }
}
