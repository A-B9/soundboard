package com.soundboard.soundboard.unit.bootstrap;

import com.soundboard.soundboard.audit.AuditLogger;
import com.soundboard.soundboard.bootstrap.SuperAdminBootstrapper;
import com.soundboard.soundboard.config.AdminProperties;
import com.soundboard.soundboard.config.BootstrapProperties;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestSuperAdminBootstrapper {

    @Mock private MyUserRepo userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Environment env;
    @Mock private ApplicationArguments args;
    @Mock private AuditLogger auditLogger;

    private SuperAdminBootstrapper bootstrapper(String username, String password, boolean forceChange) {
        BootstrapProperties bp = new BootstrapProperties(username, password);
        AdminProperties ap = new AdminProperties(forceChange);
        return new SuperAdminBootstrapper(bp, ap, userRepo, passwordEncoder, env, auditLogger);
    }

    @BeforeEach
    void defaultEnv() {
        when(env.getActiveProfiles()).thenReturn(new String[]{});
    }

    // --- Skip cases ---

    @Test
    void run_skipsSeeding_whenUsernameIsBlank() throws Exception {
        bootstrapper("", "SomePass123!", false).run(args);

        verifyNoInteractions(userRepo);
    }

    @Test
    void run_skipsSeeding_whenPasswordIsBlank() throws Exception {
        bootstrapper("superadmin", "", false).run(args);

        verifyNoInteractions(userRepo);
    }

    @Test
    void run_skipsSeeding_whenCredentialsAreNull() throws Exception {
        bootstrapper(null, null, false).run(args);

        verifyNoInteractions(userRepo);
    }

    @Test
    void run_skipsSeeding_whenSuperAdminAlreadyExists() throws Exception {
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(true);

        bootstrapper("superadmin", "SomePass123!", false).run(args);

        verify(userRepo, never()).save(any());
    }

    // --- Creation cases ---

    @Test
    void run_createsSuperAdmin_withCorrectRole() throws Exception {
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        bootstrapper("superadmin", "SomePass123!", false).run(args);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.SUPER_ADMIN);
    }

    @Test
    void run_createsSuperAdmin_withCorrectUsername() throws Exception {
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        bootstrapper("superadmin", "SomePass123!", false).run(args);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("superadmin");
    }

    @Test
    void run_encodePassword_beforeSaving() throws Exception {
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("SomePass123!")).thenReturn("bcrypt-hash");

        bootstrapper("superadmin", "SomePass123!", false).run(args);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("bcrypt-hash");
        assertThat(captor.getValue().getPassword()).isNotEqualTo("SomePass123!");
    }

    @Test
    void run_setsMustChangePassword_true_whenForceChangeEnabled() throws Exception {
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        bootstrapper("superadmin", "SomePass123!", true).run(args);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().isMustChangePassword()).isTrue();
    }

    @Test
    void run_setsMustChangePassword_false_whenForceChangeDisabled() throws Exception {
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        bootstrapper("superadmin", "SomePass123!", false).run(args);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().isMustChangePassword()).isFalse();
    }

    @Test
    void run_createsSuperAdmin_withActiveTrue() throws Exception {
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        bootstrapper("superadmin", "SomePass123!", false).run(args);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
    }

    // --- Prod fail-closed cases ---

    @Test
    void run_throwsIllegalState_inProd_whenUsernameIsMissing() {
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        assertThatThrownBy(() -> bootstrapper("", "SomePass123!", true).run(args))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_BOOTSTRAP_USERNAME");
    }

    @Test
    void run_throwsIllegalState_inProd_whenPasswordIsMissing() {
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        assertThatThrownBy(() -> bootstrapper("superadmin", "", true).run(args))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_BOOTSTRAP_PASSWORD");
    }

    @Test
    void run_throwsIllegalState_inProd_whenBothCredentialsMissing() {
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        assertThatThrownBy(() -> bootstrapper(null, null, true).run(args))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void run_doesNotThrow_inProd_whenCredentialsPresent_andSuperAdminExists() throws Exception {
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(userRepo.existsByRole(Role.SUPER_ADMIN)).thenReturn(true);

        bootstrapper("superadmin", "SomePass123!", true).run(args);

        verify(userRepo, never()).save(any());
    }
}
