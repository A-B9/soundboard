package com.soundboard.soundboard.integration.controller.sound;

import com.soundboard.soundboard.web.SoundController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SoundController.class)
@AutoConfigureMockMvc
public class GetTests {
  
  @Autowired
  MockMvc mockMvc;
  
//  @Test
//  void someTest() {
//    mockMvc.perform(post()
//                    .content().contentType())
//            .andExpect();
//  }
  
  
}
