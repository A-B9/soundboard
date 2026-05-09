package com.soundboard.soundboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SoundNotFoundException extends ResponseStatusException {
    public SoundNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Sound not found: " + id);
    }
}
