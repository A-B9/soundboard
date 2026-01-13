package com.soundboard.soundboard.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "Sounds")
public class SoundDTO {
    
    public SoundDTO() {}
    public SoundDTO(String name, String description, char keyBinding,
                    long durationOfSound, boolean active,
                    List<String> categories) {
        this.name = name;
        this.description = description;
        this.keyBinding = keyBinding;
        this.durationOfSound = durationOfSound;
        this.active = active;
        this.categories = categories;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String description;
    
    private Instant createdAt;
    
    private List<String> categories;

    private char keyBinding;
    
    @Column(name = "duration_seconds")
    private long durationOfSound;
    
    private Boolean active;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getCategories() {
        return categories;
    }
    
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    
    public char getKeyBinding() {
        return keyBinding;
    }
    
    public void setKeyBinding(char keyBinding) {
        this.keyBinding = keyBinding;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Duration getDurationOfSound() {
        return Duration.ofSeconds(durationOfSound);
    }
    
    public void setDurationOfSound(Duration durationOfSound) {
        this.durationOfSound = durationOfSound.getSeconds();
    }
    
    public Long getId() {
        return id;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
