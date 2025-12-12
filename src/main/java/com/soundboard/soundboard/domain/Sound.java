package com.soundboard.soundboard.domain;

import jakarta.persistence.*;
import lombok.Data;

//@Getter // can also be swapped with @Data
//@Setter
@Data
@Entity
@Table(name = "sounds")
public class Sound {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String filePath;

    private String keyBinding;

    private Integer volume;

    private Boolean active;

    @Override
    public String toString() {
        return "Sound [id=" + id + ", name=" + name + ", filePath=" + filePath + ", keyBinding=" + keyBinding
                + ", volume=" + volume + ", active=" + active + "]";
    }
}
