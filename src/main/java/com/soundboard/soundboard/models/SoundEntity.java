package com.soundboard.soundboard.models;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Sounds",
        indexes = {
            @Index(name = "idx_sounds_owned_by", columnList = "ownedBy")
        }
)
@Setter
@Getter
public class SoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String description;
    private String contentType;
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
                       String contentType,
                       Instant createdAt, String storedName,
                       String ownedBy, long size) {
        this.name = name;
        this.description = description;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.storedName = storedName;
        this.ownedBy = ownedBy;
        this.size = size;
    }
    
    public List<String> getTags() {
        if  (tags == null) {
            tags = new ArrayList<>();
        }
        return List.copyOf(tags);
    }
    
    public void setTags(List<String> tags) {
        if  (tags == null) {
            this.tags = new ArrayList<>();
        } else {
            this.tags = new ArrayList<>(tags);
        }
    }

}
