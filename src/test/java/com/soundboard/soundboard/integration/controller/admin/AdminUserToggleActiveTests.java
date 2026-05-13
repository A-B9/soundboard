package com.soundboard.soundboard.integration.controller.admin;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserToggleActiveTests extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SoundSeeder seeder;
    @Autowired private TestJwtHelper jwtHelper;
    @Autowired private MyUserRepo userRepo;

    @BeforeEach
    void setUp() { seeder.clearAll(); }

    @Test
    void toggleActive_admin_deactivatesActiveUser() throws Exception {
        Users target = seeder.seedUser("user-alice");
        seeder.seedAdmin("admin-bob");
        String token = jwtHelper.generateTokenForUser("admin-bob", Role.ADMIN);

        mockMvc.perform(patch("/api/soundboard/admin/users/" + target.getId() + "/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user-alice"));

        assertThat(userRepo.findById(target.getId()).get().isActive()).isFalse();
    }

    @Test
    void toggleActive_admin_reactivatesInactiveUser() throws Exception {
        Users target = seeder.seedUser("user-alice");
        target.setActive(false);
        userRepo.save(target);
        seeder.seedAdmin("admin-bob");
        String token = jwtHelper.generateTokenForUser("admin-bob", Role.ADMIN);

        mockMvc.perform(patch("/api/soundboard/admin/users/" + target.getId() + "/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(userRepo.findById(target.getId()).get().isActive()).isTrue();
    }

    @Test
    void toggleActive_admin_returns403_whenTargetIsAdmin() throws Exception {
        Users target = seeder.seedAdmin("admin-target");
        seeder.seedAdmin("admin-caller");
        String token = jwtHelper.generateTokenForUser("admin-caller", Role.ADMIN);

        mockMvc.perform(patch("/api/soundboard/admin/users/" + target.getId() + "/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleActive_superAdmin_canToggleAdminAccount() throws Exception {
        Users target = seeder.seedAdmin("admin-target");
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(patch("/api/soundboard/admin/users/" + target.getId() + "/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(userRepo.findById(target.getId()).get().isActive()).isFalse();
    }

    @Test
    void toggleActive_superAdmin_returns409_whenTogglingOwnAccount() throws Exception {
        Users self = seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(patch("/api/soundboard/admin/users/" + self.getId() + "/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }
}
