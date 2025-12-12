package com.soundboard.soundboard.web;

import com.soundboard.soundboard.service.SoundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sounds")
public class SoundController {

    private final SoundService soundService;

    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

    @PostMapping("/{id}/play")
    public ResponseEntity<Void> play(@PathVariable long id) {
//        soundService.playSound(id);
        return ResponseEntity.ok().build();
    }

    // TODO: add CRUD endpoints for Sound entity

}
