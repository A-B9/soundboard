package com.soundboard.soundboard.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Entity
@Table(name = "Sounds")
@Setter
@Getter
public class SoundEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String contentType;
    @Lob
    private byte[] audioFile;
    private Instant createdAt;
    private String storedName;
    private long size;
    
    public SoundEntity() {}
    
    @Builder
    public SoundEntity(String name, String description,
                       String contentType, byte[] audioFile,
                       Instant createdAt, String storedName,
                       long size) {
        this.name = name;
        this.description = description;
        this.contentType = contentType;
        this.audioFile = audioFile;
        this.createdAt = createdAt;
        this.storedName = storedName;
        this.size = size;
    }
    
}
