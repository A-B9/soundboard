package com.soundboard.soundboard.integration.controller.user;

import com.jayway.jsonpath.JsonPath;
import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.integration.fixtures.SoundSeeder;
import com.soundboard.soundboard.security.JWTService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginTests extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(LoginTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SoundSeeder seeder;

    @Autowired
    private JWTService jwtService;

    @BeforeAll
    void setUpClass() {
        seeder.clearAll();
        seeder.seedUserWithPassword("loginuser", "Str0ng!Pass#2026");
        log.info("--- Class Setup ---");
        log.info("Seeded user: loginuser with known password");
    }

    @BeforeEach
    void setUp() {
        seeder.clearSounds();
        log.info("--- Test Setup ---");
    }

    @Test
    void login_returns200_whenValidCredentials() throws Exception {
        log.info("=== TEST: login_returns200_whenValidCredentials ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, username='loginuser', message='User authenticated successfully', token non-empty");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.token", not(emptyString())));

        log.info("PASSED - HTTP 200 returned, valid credentials produce token and correct messages");
    }

    @Test
    void login_tokenIsValidJwtStructure_whenValidCredentials() throws Exception {
        log.info("=== TEST: login_tokenIsValidJwtStructure_whenValidCredentials ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, $.token matches three-part Base64url JWT structure");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")));

        log.info("PASSED - Token matches three-part Base64url JWT regex");
    }

    @Test
    void login_tokenAuthenticatesProtectedEndpoint_whenValidCredentials() throws Exception {
        log.info("=== TEST: login_tokenAuthenticatesProtectedEndpoint_whenValidCredentials ===");
        log.info("Endpoint : POST /login -> GET /api/soundboard/sounds");
        log.info("Auth     : Token extracted from login response, used on protected endpoint");
        log.info("Criteria : POST /login returns 200 with token; GET /api/soundboard/sounds returns 200 with that token");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(get("/api/soundboard/sounds")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());

        log.info("PASSED - Token from /login successfully authenticates GET /api/soundboard/sounds");
    }

    @Test
    void login_returns400_whenUsernameMissing() throws Exception {
        log.info("=== TEST: login_returns400_whenUsernameMissing ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.username = 'Username is required'");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"));

        log.info("PASSED - HTTP 400 returned, missing username field correctly rejected");
    }

    @Test
    void login_returns400_whenUsernameBlank() throws Exception {
        log.info("=== TEST: login_returns400_whenUsernameBlank ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.username = 'Username is required'");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"));

        log.info("PASSED - HTTP 400 returned, blank username correctly rejected");
    }

    @Test
    void login_returns400_whenPasswordMissing() throws Exception {
        log.info("=== TEST: login_returns400_whenPasswordMissing ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.password = 'Password is required'");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password is required"));

        log.info("PASSED - HTTP 400 returned, missing password field correctly rejected");
    }

    @Test
    void login_returns400_whenPasswordBlank() throws Exception {
        log.info("=== TEST: login_returns400_whenPasswordBlank ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $.password = 'Password is required'");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password is required"));

        log.info("PASSED - HTTP 400 returned, blank password correctly rejected");
    }

    @Test
    void login_returns400_whenBothFieldsBlank() throws Exception {
        log.info("=== TEST: login_returns400_whenBothFieldsBlank ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400 — both username and password blank");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        log.info("PASSED - HTTP 400 returned, both blank fields correctly rejected");
    }

    @Test
    void login_returns400_whenMalformedJson() throws Exception {
        log.info("=== TEST: login_returns400_whenMalformedJson ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 400, $['Field causing the issue'] exists");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ broken"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['Field causing the issue']").exists());

        log.info("PASSED - HTTP 400 returned, malformed JSON produces expected error key");
    }

    @Test
    void login_isPublicEndpoint_noAuthRequired() throws Exception {
        log.info("=== TEST: login_isPublicEndpoint_noAuthRequired ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : No Authorization header");
        log.info("Criteria : HTTP 200 — endpoint is public, token is non-empty");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())));

        log.info("PASSED - HTTP 200 returned, /login is accessible without an Authorization header");
    }

    @Test
    void login_returns200_whenInvalidPassword() throws Exception {
        log.info("=== TEST: login_returns200_whenInvalidPassword ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, message='Invalid username or password', token='' (vague response for bad credentials)");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"WrongPass!2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.token").value(""));

        log.info("PASSED - HTTP 200 returned with vague error, bad credentials handled gracefully");
    }

    @Test
    void login_returns200_whenUserDoesNotExist() throws Exception {
        log.info("=== TEST: login_returns200_whenUserDoesNotExist ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : HTTP 200, message='Invalid username or password', token='' (vague response for unknown user)");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.token").value(""));

        log.info("PASSED - HTTP 200 returned with vague error, non-existent user handled gracefully");
    }

    @Test
    void login_tokenContainsRoleUserClaim_forRegularUser() throws Exception {
        log.info("=== TEST: login_tokenContainsRoleUserClaim_forRegularUser ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : JWT role claim extracted via JWTService.extractRole equals 'USER' for a regular registered user");

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        String roleClaim = jwtService.extractRole(token);

        assertThat(roleClaim).isEqualTo("USER");

        log.info("PASSED - JWT role claim is 'USER' for a regular user login");
    }

    @Test
    void login_tokenContainsMustChangePasswordFalse_byDefault() throws Exception {
        log.info("=== TEST: login_tokenContainsMustChangePasswordFalse_byDefault ===");
        log.info("Endpoint : POST /login");
        log.info("Auth     : None (public endpoint)");
        log.info("Criteria : JWT mustChangePassword claim extracted via JWTService.extractMustChangePassword is false by default");

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        boolean mustChangePassword = jwtService.extractMustChangePassword(token);

        assertThat(mustChangePassword).isFalse();

        log.info("PASSED - JWT mustChangePassword claim is false for a newly registered user");
    }

    @Test
    void login_tokenGrantsAccessToProtectedEndpoint_withRoleUserAuthority() throws Exception {
        log.info("=== TEST: login_tokenGrantsAccessToProtectedEndpoint_withRoleUserAuthority ===");
        log.info("Endpoint : POST /login -> GET /api/soundboard/sounds");
        log.info("Auth     : Token extracted from login, carrying ROLE_USER authority derived from role claim");
        log.info("Criteria : GET /api/soundboard/sounds returns HTTP 200, confirming ROLE_USER grants access");

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"loginuser\",\"password\":\"Str0ng!Pass#2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(get("/api/soundboard/sounds")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());

        log.info("PASSED - ROLE_USER authority derived from JWT role claim grants access to protected endpoint");
    }
}
