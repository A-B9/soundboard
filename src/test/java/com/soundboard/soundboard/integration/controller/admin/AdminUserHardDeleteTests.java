package com.soundboard.soundboard.integration.controller.admin;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.repository.SoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserHardDeleteTests extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SoundSeeder seeder;
    @Autowired private TestJwtHelper jwtHelper;
    @Autowired private MyUserRepo userRepo;
    @Autowired private SoundRepository soundRepository;

    @BeforeEach
    void setUp() { seeder.clearAll(); }

    @Test
    void hardDelete_superAdmin_returns204_andRemovesUser() throws Exception {
        Users target = seeder.seedUser("user-to-delete");
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(delete("/api/soundboard/admin/users/" + target.getId() + "/hard-delete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepo.findById(target.getId())).isEmpty();
    }

    @Test
    void hardDelete_cascadesSoundsFromDb() throws Exception {
        Users target = seeder.seedUser("user-with-sounds");
        seeder.seedSound("sound-one", target.getUsername());
        seeder.seedSound("sound-two", target.getUsername());
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(delete("/api/soundboard/admin/users/" + target.getId() + "/hard-delete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(soundRepository.findAllByOwnedBy(target.getUsername())).isEmpty();
        assertThat(userRepo.findById(target.getId())).isEmpty();
    }

    @Test
    void hardDelete_returns409_whenTargetIsSelf() throws Exception {
        Users self = seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(delete("/api/soundboard/admin/users/" + self.getId() + "/hard-delete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        assertThat(userRepo.findById(self.getId())).isPresent();
    }

    @Test
    // The 1→0 case (deleting the truly last SA) is already blocked by the self-check
    // because the only caller who could make this request is also a SA, making them the target.
    // Unit tests cover the count guard. Integration verifies the allowed 2→1 case succeeds.
    void hardDelete_allows_deletingOneSuperAdmin_whenAnotherRemains() throws Exception {
        Users otherSa = seeder.seedSuperAdmin("other-sa");
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(delete("/api/soundboard/admin/users/" + otherSa.getId() + "/hard-delete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepo.findById(otherSa.getId())).isEmpty();
        assertThat(userRepo.countByRole(Role.SUPER_ADMIN)).isEqualTo(1L);
    }

    @Test
    void hardDelete_superAdmin_canDeleteAnotherSuperAdmin_whenMultipleExist() throws Exception {
        Users otherSa = seeder.seedSuperAdmin("other-sa");
        seeder.seedSuperAdmin("sa-caller");
        String token = jwtHelper.generateTokenForUser("sa-caller", Role.SUPER_ADMIN);

        mockMvc.perform(delete("/api/soundboard/admin/users/" + otherSa.getId() + "/hard-delete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepo.findById(otherSa.getId())).isEmpty();
    }

    @Test
    void hardDelete_returns403_whenCallerIsAdmin() throws Exception {
        Users target = seeder.seedUser("user-target");
        seeder.seedAdmin("admin-caller");
        String token = jwtHelper.generateTokenForUser("admin-caller", Role.ADMIN);

        mockMvc.perform(delete("/api/soundboard/admin/users/" + target.getId() + "/hard-delete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        assertThat(userRepo.findById(target.getId())).isPresent();
    }
}
