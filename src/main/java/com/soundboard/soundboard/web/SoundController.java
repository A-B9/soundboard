package com.soundboard.soundboard.web;

import com.soundboard.soundboard.domain.entities.SoundDTO;
import com.soundboard.soundboard.domain.responses.SoundResponse;
import com.soundboard.soundboard.domain.responses.SoundResponse.SoundData;
import com.soundboard.soundboard.service.SoundService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/soundboard")
public class SoundController {
    
    private final SoundService soundService;

    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

//    @PostMapping("/api/sound/{id}/play")
//    public ResponseEntity<Void> play(@PathVariable long id) {
////        soundService.playSound(id);
//        return ResponseEntity.ok().build();
//    }
    
    @PostMapping("/sounds")
    public ResponseEntity<SoundResponse> createSound(@RequestBody SoundDTO sound) {
        soundService.createSound(sound);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(SoundResponse.success(
                        SoundData.createData(sound)
                        )
                );
    }
    
    @GetMapping("/sounds")
    @ResponseBody
    public ResponseEntity<List<SoundDTO>> getAllSounds() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(soundService.getAllSounds()
                );
    }
    
    @GetMapping("/sounds/{id}")
    public ResponseEntity<SoundDTO> getSound(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(soundService.getSoundById(id)
                );
    }
    
    @DeleteMapping("/sounds/{id}")
    public ResponseEntity<SoundDTO> deleteSound(@PathVariable Long id) {
        soundService.deleteSound(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(null);
    }

}
