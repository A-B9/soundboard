package com.soundboard.soundboard.integration.controller.admin;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserGetTests extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SoundSeeder seeder;
    @Autowired private TestJwtHelper jwtHelper;

    @BeforeEach
    void setUp() { seeder.clearAll(); }

    @Test
    void getUser_admin_returns200_forUserRoleTarget() throws Exception {
        Users target = seeder.seedUser("user-alice");
        seeder.seedAdmin("admin-bob");
        String token = jwtHelper.generateTokenForUser("admin-bob", Role.ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users/" + target.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user-alice"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUser_admin_returns403_forAdminRoleTarget() throws Exception {
        Users target = seeder.seedAdmin("admin-target");
        seeder.seedAdmin("admin-caller");
        String token = jwtHelper.generateTokenForUser("admin-caller", Role.ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users/" + target.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_admin_returns403_forSuperAdminRoleTarget() throws Exception {
        Users target = seeder.seedSuperAdmin("sa-target");
        seeder.seedAdmin("admin-caller");
        String token = jwtHelper.generateTokenForUser("admin-caller", Role.ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users/" + target.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_superAdmin_returns200_forAdminRoleTarget() throws Exception {
        Users target = seeder.seedAdmin("admin-target");
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users/" + target.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin-target"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void getUser_returns404_whenUserDoesNotExist() throws Exception {
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
