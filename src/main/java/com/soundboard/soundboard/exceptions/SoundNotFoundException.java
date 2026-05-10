package com.soundboard.soundboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class SoundNotFoundException extends ResponseStatusException {
    public SoundNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Sound not found: " + id);
    }
}
