package com.soundboard.soundboard.models;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

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
    private String ownedBy;
    private boolean active = true;
    private SoundCategoryEnum category;
    @ElementCollection
    @CollectionTable(name = "sound_tags", joinColumns = @JoinColumn(name = "sound_id"))
    @Column(name = "tag")
    private List<String> tags;
    private Instant recentUpdate;

    public SoundEntity() {}

    @Builder
    public SoundEntity(String name, String description,
                       String contentType, byte[] audioFile,
                       Instant createdAt, String storedName,
                       String ownedBy, long size) {
        this.name = name;
        this.description = description;
        this.contentType = contentType;
        this.audioFile = audioFile;
        this.createdAt = createdAt;
        this.storedName = storedName;
        this.ownedBy = ownedBy;
        this.size = size;
    }

    public byte[] getAudioFile() {
        return audioFile != null ? audioFile.clone() : null;
    }
    public void setAudioFile(byte[] audioFile) {
        this.audioFile = audioFile != null ? audioFile.clone() : null;
    }

}
