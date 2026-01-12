package com.soundboard.soundboard.web;

import com.soundboard.soundboard.domain.Sound;
import com.soundboard.soundboard.service.SoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sounds")
public class SoundController {

    @Autowired
    private final SoundService soundService;

    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

//    @PostMapping("/api/sound/{id}/play")
//    public ResponseEntity<Void> play(@PathVariable long id) {
////        soundService.playSound(id);
//        return ResponseEntity.ok().build();
//    }
    
    @PostMapping("/api/sound")
    public Sound createSound(@RequestBody Sound sound) {
        return soundService.createSound(sound);
    }
    
    @GetMapping("/api/sound")
    public List<Sound> getAllSounds() {
        return soundService.getAllSounds();
    }
    
    @GetMapping("/api/sound/{id}")
    public Sound getSound(@PathVariable Long id) {
        return soundService.getSoundById(id);
    }
    
    @DeleteMapping("/api/sound/{id}")
    public void deleteSound(@PathVariable Long id) {
        soundService.deleteSound(id);
    }

}
