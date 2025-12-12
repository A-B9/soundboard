package com.soundboard.soundboard.audio;

import javax.sound.sampled.LineUnavailableException;

public interface AudioPlayer {

    public void playSound(String filePath) throws LineUnavailableException;
}
