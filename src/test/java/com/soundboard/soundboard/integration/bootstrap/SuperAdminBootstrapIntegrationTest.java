package com.soundboard.soundboard.integration.bootstrap;

import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "app.bootstrap.username=bootstrap-superadmin",
        "app.bootstrap.password=BootstrapPass123!"
})
class SuperAdminBootstrapIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MyUserRepo userRepo;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void bootstrapper_createsSuperAdmin_onContextStartup() {
        assertThat(userRepo.existsByRole(Role.SUPER_ADMIN)).isTrue();
    }

    @Test
    void bootstrappedSuperAdmin_hasCorrectUsername() {
        Users superAdmin = userRepo.findByUsername("bootstrap-superadmin");

        assertThat(superAdmin).isNotNull();
        assertThat(superAdmin.getRole()).isEqualTo(Role.SUPER_ADMIN);
        assertThat(superAdmin.isActive()).isTrue();
    }

    @Test
    void bootstrappedSuperAdmin_hasMustChangePassword_true_underTestProfile() {
        // test profile sets app.admin.force-password-change=true — mirrors prod behaviour
        Users superAdmin = userRepo.findByUsername("bootstrap-superadmin");

        assertThat(superAdmin.isMustChangePassword()).isTrue();
    }

    @Test
    void bootstrappedSuperAdmin_passwordIsEncoded_notStoredPlaintext() {
        Users superAdmin = userRepo.findByUsername("bootstrap-superadmin");

        assertThat(superAdmin.getPassword()).isNotEqualTo("BootstrapPass123!");
        assertThat(superAdmin.getPassword()).startsWith("$2a$");
    }

    @Test
    void bootstrappedSuperAdmin_canAuthenticateViaLogin() throws Exception {
        mockMvc.perform(post("/api/soundboard/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bootstrap-superadmin","password":"BootstrapPass123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.message").value("User authenticated successfully"));
    }
}
