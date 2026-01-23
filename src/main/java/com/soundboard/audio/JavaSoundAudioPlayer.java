package com.soundboard.audio;

import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

@Component
public class JavaSoundAudioPlayer implements AudioPlayer {

    @Override
    public void playSound(String filePath) throws LineUnavailableException {
        /// AudioInputStream and Clip implement AutoCloseable, so they will be closed automatically.
        /// AudioInputStream allows us to read audio data from a file.
        /// A Clip is a special kind of data line that can be loaded with audio data and then played back.
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException e) {
            ///  add logging into this section.
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
