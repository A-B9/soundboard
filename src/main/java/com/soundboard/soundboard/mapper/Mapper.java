package com.soundboard.soundboard.mapper;

import com.soundboard.soundboard.models.SoundDTO;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.GetSoundResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Component
public class Mapper  {
  public SoundDTO toSoundDTO(SoundEntity entity) {
    return new SoundDTO(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedAt(),
            entity.getStoredName(),
            entity.getSize());
  }
  
  public SoundEntity toEntity(SoundRequestModel soundRequestModel, MultipartFile file) throws IOException {
    return new SoundEntity(
            soundRequestModel.name(),
            soundRequestModel.description(),
            file.getContentType(),
            file.getBytes(),
            Instant.now(),
            null,
            file.getSize());
  }
  
  public GetSoundResponse toGetResponse(SoundEntity entity) {
    return new GetSoundResponse(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getContentType(),
            entity.getSize()
    );
  }
}
