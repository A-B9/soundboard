package com.soundboard.soundboard.service;

import com.soundboard.soundboard.audio.AudioPlayer;
import com.soundboard.soundboard.domain.Sound;
import com.soundboard.soundboard.repository.SoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service // Indicates that this class is a service component in Spring, making it eligible for component scanning and dependency injection
// Service classes typically contain business logic and interact with repositories to manage data
public class SoundService {

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
        try {
            Files.createDirectories(soundDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not create sounds directory: " + soundDirectory, e);
        }
    }

    //TODO: implement repo functionality.
    public void playSound(Long id) throws LineUnavailableException {
        Sound sound = soundRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sound not found with id: " + id));
        audioPlayer.playSound(sound.getFilePath());
    }

    public List<Sound> getAllSounds() {
        return soundRepository.findAll();
    }

    public Sound getSoundById(Long id) {
        return soundRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sound not found with id: " + id));
    }

}
