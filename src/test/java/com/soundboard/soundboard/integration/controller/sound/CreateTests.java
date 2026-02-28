package com.soundboard.soundboard.integration.controller.sound;

import com.soundboard.soundboard.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CreateTests extends BaseIntegrationTest {
  
  private String token;
  
  @BeforeEach
  void setUp() throws Exception {
    String username = "testUser_" + UUID.randomUUID(); // unique user between tests to prevent conflicts, ensures tests are isolated and can run in any order without affecting each other
    token = registerTestUserAndLogin(username);
  }
  
  @Test
  void createSound_shouldReturn201() throws Exception {
    MockMultipartFile mockedFile = new MockMultipartFile(
            "file",
            "testSound.mp3",
            "audio/mp3",
            "fake audio content".getBytes()
    );
    
    String soundRequestJson = """
            {
              "name": "Test Sound",
              "description": "A sound for testing"
            }
            """;
  
    MockMultipartFile soundRequest = new MockMultipartFile(
            "soundRequest",
            "",
            "application/json",
            soundRequestJson.getBytes()
    );
    
    mockMvc.perform(
            multipart("/api/soundboard/sounds")
                    .file(mockedFile)
                    .file(soundRequest)
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Test Sound"))
            .andExpect(jsonPath("$.description").value("A sound for testing"));
  }
  
  @Test
  void createSound_InvalidFile_shouldReturn() {
  
  }
  
  @Test
  void createSound_MissingName_shouldReturn() {
  
  }


}
