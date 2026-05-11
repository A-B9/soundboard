package com.soundboard.soundboard.integration.controller.user;

import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegisterTests extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(RegisterTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SoundSeeder seeder;

    @Autowired
    private MyUserRepo userRepo;

    @BeforeEach
    void setUp() {
        seeder.clearAll();
        log.info("--- Test Setup ---");
    }

    @Test
    void register_returns200_whenValidRequest() throws Exception {
        log.info("=== TEST: register_returns200_whenValidRequest ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, username='newuser', message='User registered successfully'");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        log.info("PASSED - HTTP 200 returned, user 'newuser' registered successfully");
    }

    @Test
    void register_persistsBcryptHash_whenValidRequest() throws Exception {
        log.info("=== TEST: register_persistsBcryptHash_whenValidRequest ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, persisted password is BCrypt hash (starts with $2a$ or $2b$), not plain text");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bcryptcheck\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        Users saved = userRepo.findByUsername("bcryptcheck");
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo("Str0ng!Pass#2026");
        assertThat(saved.getPassword()).matches("^\\$2[ab]\\$.*");

        log.info("PASSED - Persisted password is BCrypt hash, plain-text password not stored");
    }

    @Test
    void register_returns400_whenUsernameAlreadyExists() throws Exception {
        log.info("=== TEST: register_returns400_whenUsernameAlreadyExists ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, username='existinguser', message='Username already exists'");

        seeder.seedUser("existinguser");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"existinguser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("existinguser"))
                .andExpect(jsonPath("$.message").value("Username already exists"));

        log.info("PASSED - HTTP 400 returned, duplicate username rejected with correct message");
    }

    @Test
    void register_returns400_whenUsernameBlank() throws Exception {
        log.info("=== TEST: register_returns400_whenUsernameBlank ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.username validation message = 'Username is required'");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"));

        log.info("PASSED - HTTP 400 returned, blank username correctly rejected");
    }

    @Test
    void register_returns400_whenUsernameMissing() throws Exception {
        log.info("=== TEST: register_returns400_whenUsernameMissing ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.username validation message = 'Username is required'");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"));

        log.info("PASSED - HTTP 400 returned, missing username field correctly rejected");
    }

    @Test
    void register_returns400_whenUsernameWhitespaceOnly() throws Exception {
        log.info("=== TEST: register_returns400_whenUsernameWhitespaceOnly ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.username validation message = 'Username is required'");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"   \",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"));

        log.info("PASSED - HTTP 400 returned, whitespace-only username correctly rejected");
    }

    @Test
    void register_returns400_whenPasswordBlank() throws Exception {
        log.info("=== TEST: register_returns400_whenPasswordBlank ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400 — multiple validators may fire; status assertion only");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u1\",\"password\":\"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        log.info("PASSED - HTTP 400 returned, blank password correctly rejected");
    }

    @Test
    void register_returns400_whenPasswordMissing() throws Exception {
        log.info("=== TEST: register_returns400_whenPasswordMissing ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.password = 'Password is required'");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u1\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password is required"));

        log.info("PASSED - HTTP 400 returned, missing password field correctly rejected");
    }

    @Test
    void register_returns400_whenPasswordShorterThan12Chars() throws Exception {
        log.info("=== TEST: register_returns400_whenPasswordShorterThan12Chars ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.password = 'Password must be at least 12 characters long' (8-char password)");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u2\",\"password\":\"Sh0rt!Pw\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must be at least 12 characters long"));

        log.info("PASSED - HTTP 400 returned, 8-char password correctly rejected with length message");
    }

    @Test
    void register_returns400_whenPasswordExactly11Chars() throws Exception {
        log.info("=== TEST: register_returns400_whenPasswordExactly11Chars ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.password = 'Password must be at least 12 characters long' (11-char password)");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u3\",\"password\":\"Eleven!Pas1\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must be at least 12 characters long"));

        log.info("PASSED - HTTP 400 returned, 11-char password (boundary) correctly rejected");
    }

    @Test
    void register_returns200_whenPasswordExactly12Chars() throws Exception {
        log.info("=== TEST: register_returns200_whenPasswordExactly12Chars ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, message='User registered successfully' (exactly 12-char password with special char)");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u4\",\"password\":\"Twelve!Pass1\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        log.info("PASSED - HTTP 200 returned, 12-char boundary password accepted");
    }

    @Test
    void register_returns400_whenPasswordNoSpecialChar() throws Exception {
        log.info("=== TEST: register_returns400_whenPasswordNoSpecialChar ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.password = 'Password must contain at least one special character' (17-char alphanumeric)");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u5\",\"password\":\"AlphanumericOnly1\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must contain at least one special character"));

        log.info("PASSED - HTTP 400 returned, password without special character correctly rejected");
    }

    @Test
    void register_returns400_whenPasswordFailsMultipleRules() throws Exception {
        log.info("=== TEST: register_returns400_whenPasswordFailsMultipleRules ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.password exists (5-char password fails both length and special-char rules)");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u6\",\"password\":\"short\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());

        log.info("PASSED - HTTP 400 returned, password failing multiple rules correctly rejected");
    }

    @Test
    void register_returns400_whenMalformedJson() throws Exception {
        log.info("=== TEST: register_returns400_whenMalformedJson ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $['Field causing the issue'] exists");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not-json"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['Field causing the issue']").exists());

        log.info("PASSED - HTTP 400 returned, malformed JSON produces expected error key");
    }

    @Test
    void register_isPublicEndpoint_noAuthRequired() throws Exception {
        log.info("=== TEST: register_isPublicEndpoint_noAuthRequired ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : No Authorization header");
        log.info("Criteria : HTTP 200 — endpoint is public, no token should be required");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"publicuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        log.info("PASSED - HTTP 200 returned, /register is accessible without an Authorization header");
    }

    @Test
    void register_responseContainsRoleUser_onSuccess() throws Exception {
        log.info("=== TEST: register_responseContainsRoleUser_onSuccess ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, $.role = 'USER' in response body");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rolecheck1\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));

        log.info("PASSED - HTTP 200 returned, $.role is 'USER' in the registration response");
    }

    @Test
    void register_persistsDefaultRole_asUser() throws Exception {
        log.info("=== TEST: register_persistsDefaultRole_asUser ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, persisted Users entity has role == Role.USER");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rolecheck2\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        Users saved = userRepo.findByUsername("rolecheck2");
        assertThat(saved).isNotNull();
        assertThat(saved.getRole()).isEqualTo(Role.USER);

        log.info("PASSED - Persisted user entity has Role.USER as the default role");
    }

    @Test
    void register_roleIsUser_notNullInResponse() throws Exception {
        log.info("=== TEST: register_roleIsUser_notNullInResponse ===");
        log.info("Endpoint : POST /register");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, $.role is present and not null in response body");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rolecheck3\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.role").isNotEmpty());

        log.info("PASSED - HTTP 200 returned, $.role field is present and non-null in the registration response");
    }
}
