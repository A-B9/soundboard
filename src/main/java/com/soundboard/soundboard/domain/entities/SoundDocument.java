package com.soundboard.soundboard.domain.entities;

import com.soundboard.soundboard.util.SoundCategoryEnum;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

//TODO: Needs more investigation to implement DTO pattern
// Do not wire into the application
public class SoundDocument {
  
  public SoundDocument() {}
  
  public SoundDocument(Long id, String name, String description,
                       List<SoundCategoryEnum> categories,
                       char keyBinding, boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.categories = categories;
    this.keyBinding = keyBinding;
    this.active = active;
  }
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private String name;
  
  private String description;
  
  private Instant createdAt;
  
  private List<SoundCategoryEnum> categories;
  
  private char keyBinding;
  
  private Duration durationOfSound;
  
  private boolean active;
  
  private String filePath;
  
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public Instant getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
  
  public List<SoundCategoryEnum> getCategories() {
    return categories;
  }
  
  public void setCategories(List<SoundCategoryEnum> categories) {
    this.categories = categories;
  }
  
  public char getKeyBinding() {
    return keyBinding;
  }
  
  public void setKeyBinding(char keyBinding) {
    this.keyBinding = keyBinding;
  }
  
  public boolean isActive() {
    return active;
  }
  
  public void setActive(boolean active) {
    this.active = active;
  }
  
  public String getFilePath() {
    return filePath;
  }
  
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
  
  public Duration getDurationOfSound() {
    return durationOfSound;
  }
  
  public void setDurationOfSound(Duration durationOfSound) {
    this.durationOfSound = durationOfSound;
  }
}
