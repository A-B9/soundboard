package com.soundboard.soundboard.integration.controller.admin;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.repository.MyUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserCreateTests extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SoundSeeder seeder;
    @Autowired private TestJwtHelper jwtHelper;
    @Autowired private MyUserRepo userRepo;

    @BeforeEach
    void setUp() { seeder.clearAll(); }

    @Test
    void createUser_superAdmin_returns201_andUserIsPersisted() throws Exception {
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-admin","password":"SecurePass123!","role":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new-admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        assertThat(userRepo.existsByUsername("new-admin")).isTrue();
    }

    @Test
    void createUser_superAdmin_canCreateSuperAdmin() throws Exception {
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-sa","password":"SecurePass123!","role":"SUPER_ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"));
    }

    @Test
    void createUser_setsDisplayName_toUsername_whenOmitted() throws Exception {
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"no-display","password":"SecurePass123!","role":"USER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value("no-display"));
    }

    @Test
    void createUser_setsMustChangePassword_true_underTestProfile() throws Exception {
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"forced-change","password":"SecurePass123!","role":"USER"}
                                """))
                .andExpect(status().isCreated());

        assertThat(userRepo.findByUsername("forced-change").isMustChangePassword()).isTrue();
    }

    @Test
    void createUser_returns403_whenCallerIsAdmin() throws Exception {
        seeder.seedAdmin("admin-caller");
        String token = jwtHelper.generateTokenForUser("admin-caller", Role.ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-user","password":"SecurePass123!","role":"USER"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_returns409_whenUsernameAlreadyExists() throws Exception {
        seeder.seedUser("existing-user");
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"existing-user","password":"SecurePass123!","role":"USER"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_returns400_whenPasswordTooShort() throws Exception {
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"user","password":"Short1!","role":"USER"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returns400_whenPasswordHasNoSpecialChar() throws Exception {
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(post("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"user","password":"NoSpecialChar12","role":"USER"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
