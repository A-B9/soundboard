package com.soundboard.soundboard.integration.controller.user;

import com.soundboard.soundboard.TestJwtHelper;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChangePasswordTests extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SoundSeeder seeder;
    @Autowired private TestJwtHelper jwtHelper;

    @BeforeEach
    void setUp() { seeder.clearAll(); }

    @Test
    void changePassword_returns200_andNewToken_whenValid() throws Exception {
        seeder.seedUserWithMustChangePassword("alice", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("alice", Role.USER, true);

        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword1!","newPassword":"NewPassword1!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void changePassword_returns400_whenCurrentPasswordIncorrect() throws Exception {
        seeder.seedUserWithMustChangePassword("alice", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("alice", Role.USER, true);

        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"WrongPassword1!","newPassword":"NewPassword1!"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_returns400_whenNewPasswordSameAsCurrent() throws Exception {
        seeder.seedUserWithMustChangePassword("alice", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("alice", Role.USER, true);

        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword1!","newPassword":"OldPassword1!"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_returns400_whenNewPasswordTooShort() throws Exception {
        seeder.seedUserWithMustChangePassword("alice", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("alice", Role.USER, true);

        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword1!","newPassword":"short!"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_returns400_whenNewPasswordHasNoSpecialChar() throws Exception {
        seeder.seedUserWithMustChangePassword("alice", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("alice", Role.USER, true);

        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword1!","newPassword":"NoSpecialChar1234"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword1!","newPassword":"NewPassword1!"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_worksForNormalUser_whenMustChangePasswordIsFalse() throws Exception {
        seeder.seedUserWithPassword("bob", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("bob", Role.USER, false);

        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword1!","newPassword":"NewPassword1!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // --- mustChangePassword gate ---

    @Test
    void mustChangePassword_gate_blocks_otherEndpoints_returns403() throws Exception {
        seeder.seedUserWithMustChangePassword("charlie", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("charlie", Role.USER, true);

        mockMvc.perform(get("/api/soundboard/sounds")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Password change required before accessing this resource"));
    }

    @Test
    void mustChangePassword_gate_allows_passwordChangeEndpoint() throws Exception {
        seeder.seedUserWithMustChangePassword("charlie", "OldPassword1!");
        String token = jwtHelper.generateTokenForUser("charlie", Role.USER, true);

        mockMvc.perform(post("/api/soundboard/user/password-reset")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"OldPassword1!","newPassword":"NewPassword1!"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void mustChangePassword_gate_doesNotAffect_unauthenticatedRequests() throws Exception {
        mockMvc.perform(post("/api/soundboard/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"newuser","password":"ValidPassword1!"}
                                """))
                .andExpect(status().isOk());
    }
}
