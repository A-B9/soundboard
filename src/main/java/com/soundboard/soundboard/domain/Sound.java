package com.soundboard.soundboard.domain;

import com.soundboard.soundboard.util.SoundCategoryEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

//@Getter // can also be swapped with @Data
//@Setter
@Getter
@Setter
@Entity
@Table(name = "sounds")
public class Sound {
    
    public Sound() {}
    public Sound(String name, String filePath, String keyBinding, boolean active) {
        this.name = name;
        this.filePath = filePath;
        this.keyBinding = keyBinding;
        this.active = active;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String filePath;
    
    private SoundCategoryEnum categories;

    private String keyBinding;

    private Integer volume;

    private Boolean active;

    @Override
    public String toString() {
        return "Sound [id=" + id + ", name=" + name + ", filePath=" + filePath + ", keyBinding=" + keyBinding
                + ", volume=" + volume + ", active=" + active + "]";
    }
}
