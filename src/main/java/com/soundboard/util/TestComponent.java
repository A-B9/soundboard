package com.soundboard.util;

import org.springframework.stereotype.Component;

@Component
public class TestComponent {
  
  public void build() {
    System.out.println("The bean has been built");
  }
}
