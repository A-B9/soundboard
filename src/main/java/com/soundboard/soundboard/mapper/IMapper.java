package com.soundboard.soundboard.mapper;

import com.soundboard.soundboard.models.SoundDTO;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.sound.GetSoundResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper(componentModel = "spring")
public interface IMapper {
  
  @Mapping(target = "storedName", ignore = true)
  @Mapping(target = "ownedTo", ignore = true)
  @Mapping(target = "name", source = "soundRequestModel.name")
  @Mapping(target = "description", source = "soundRequestModel.description")
  @Mapping(target = "contentType", source = "file.contentType")
  @Mapping(target = "size", source = "file.size")
  @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
  @Mapping(target = "audioFile", source = "file.bytes")
  SoundEntity toEntity(SoundRequestModel soundRequestModel, MultipartFile file) throws IOException;
  
//  @Mapping(target = "id", source = "entity.id")
//  @Mapping(target = "name", source = "entity.name")
//  @Mapping(target = "description", source = "entity.description")
//  @Mapping(target = "contentType", source = "entity.contentType")
//  @Mapping(target = "size", source = "entity.size")
  GetSoundResponse toGetResponse(SoundEntity entity);
  
  
  SoundDTO toSoundDTO(SoundEntity entity);
}
