package com.soundboard.soundboard.domain.responses;

import com.soundboard.soundboard.domain.entities.SoundDTO;

import java.time.Instant;

public record SoundResponse(
        String status,
        SoundData data,
        Instant timeStamp
) {
  public static SoundResponse success(SoundData data) {
    return new SoundResponse("success", data, Instant.now());
  }
  
  public record SoundData(
          String name,
          char keyBinding
  ) {
    
    public static SoundData createData(SoundDTO sound) {
      return new SoundData(sound.getName(), sound.getKeyBinding());
    }
  }
}
