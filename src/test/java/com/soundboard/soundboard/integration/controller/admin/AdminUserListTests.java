package com.soundboard.soundboard.integration.controller.admin;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserListTests extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SoundSeeder seeder;
    @Autowired private TestJwtHelper jwtHelper;

    @BeforeEach
    void setUp() { seeder.clearAll(); }

    @Test
    void listUsers_admin_returns200_withOnlyUserRoleAccounts() throws Exception {
        seeder.seedUser("user-alice");
        seeder.seedUser("user-bob");
        seeder.seedAdmin("admin-carol");
        String token = jwtHelper.generateTokenForUser("admin-carol", Role.ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].role").value("USER"));
    }

    @Test
    void listUsers_superAdmin_returns200_withAllUsers() throws Exception {
        seeder.seedUser("user-alice");
        seeder.seedAdmin("admin-bob");
        seeder.seedSuperAdmin("sa-dave");
        String token = jwtHelper.generateTokenForUser("sa-dave", Role.SUPER_ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void listUsers_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/soundboard/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsers_returns403_whenUserRole() throws Exception {
        seeder.seedUser("regular-user");
        String token = jwtHelper.generateTokenForUser("regular-user", Role.USER);

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_admin_returnsEmptyList_whenNoUsersExist() throws Exception {
        seeder.seedAdmin("lonely-admin");
        String token = jwtHelper.generateTokenForUser("lonely-admin", Role.ADMIN);

        mockMvc.perform(get("/api/soundboard/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
