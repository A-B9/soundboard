package com.soundboard.soundboard.config;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AudioConfig {
  
  @Bean
  Tika tika() {
    return new Tika();
  }
}
