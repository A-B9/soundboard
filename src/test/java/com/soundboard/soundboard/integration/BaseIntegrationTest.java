package com.soundboard.soundboard.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.responseModels.user.LoginResponse;
import com.soundboard.soundboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // starts full application context with a real embedded server on a random port
//@Transactional
@Testcontainers // ensures that the PostgreSQL container is started before any tests run and stopped afterward, lifecycle managed by Junit
@ActiveProfiles("test") // loads test specific application propertiees
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
  // single abstract base class that all integration tests extend.
  
  @Autowired
  protected MockMvc mockMvc; // allows us to perform HTTP requests against our controllers without starting a real server, simulates requests and responses
  
  @Autowired
  protected UserService userService; // allows us to interact with the user service layer directly in our tests, useful for setting up test data or verifying results
  
  protected ObjectMapper objectMapper = new ObjectMapper(); // used to serialize and deserialize JSON in our tests, allows us to easily convert Java objects to JSON strings and vice versa
  
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("soundboard_test")
          .withUsername("test")
          .withPassword("test");
  // spin up a real postgres instance in docker during tests

  @DynamicPropertySource // injects containers connectino details into spring config at runtime. datasource points at container rather than any local db
  static void configureProperties(DynamicPropertyRegistry registry) {
    // static ensures the container is shared across all tests in class rather than restarted between each test method
      registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
      registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
      registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
      registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // ensures schema is created fresh for each test run, prevents data from previous tests affecting current ones
  }
  
  protected String registerTestUserAndLogin(String username) throws Exception {
    // helper method to register a user via the service layer, returns the registered user's username for use in tests
    Users user = Users.builder()
            .username(username)
            .password("pass123")
            .build();
    
    mockMvc.perform(
            post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk());
  
    MvcResult result = mockMvc.perform(
            post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andReturn();
  
    LoginResponse loginResponse = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
    
    
    return loginResponse.token();
  }
  
}
