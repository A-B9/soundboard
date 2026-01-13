package com.soundboard.soundboard.service;

import com.soundboard.soundboard.audio.AudioPlayer;
import com.soundboard.soundboard.domain.entities.SoundDTO;
import com.soundboard.soundboard.repository.SoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service // Indicates that this class is a service component in Spring, making it eligible for component scanning and dependency injection
// Service classes typically contain business logic and interact with repositories to manage data
public class SoundService {

    @Autowired
    private final SoundRepository soundRepository;
    private final AudioPlayer audioPlayer;
    private final Path soundDirectory;

    @Autowired
    public SoundService(SoundRepository soundRepository, AudioPlayer audioPlayer,
                        @Value("${app.sounds.directory}") String soundDirectory) {
        this.soundRepository = soundRepository;
        this.audioPlayer = audioPlayer;
        this.soundDirectory = Path.of(soundDirectory);
        initDirectory();
    }

    // Initialize the sound directory, creating it if it doesn't exist
    private void initDirectory() {
        if (!Files.exists(Path.of("./sounds"))) {
            try {
                Files.createDirectories(soundDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Could not create sounds directory: " + soundDirectory, e);
            }
        }
        
    }
//    public void playSound(Long id) throws LineUnavailableException {
//        Sound sound = soundRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Sound not found with id: " + id));
//        audioPlayer.playSound(sound.getFilePath());
//    }
    
    public void createSound(SoundDTO sound) {
        sound.setCreatedAt(Instant.now());
        soundRepository.save(sound);
    }
    
    public void deleteSound(Long id) {
        soundRepository.deleteById(id);
    }

    public List<SoundDTO> getAllSounds() {
        return soundRepository.findAll();
    }

    public SoundDTO getSoundById(Long id) {
        return soundRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sound not found with id: " + id));
    }

}
