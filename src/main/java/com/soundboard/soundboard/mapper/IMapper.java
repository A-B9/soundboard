package com.soundboard.soundboard.mapper;

import com.soundboard.soundboard.models.SoundDTO;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.UserDTO;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.sound.GetSoundResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper(componentModel = "spring")
public interface IMapper {

  @BeanMapping(builder = @Builder(disableBuilder = true))
  @Mapping(target = "storedName", ignore = true)
  @Mapping(target = "ownedBy", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "recentUpdate", ignore = true)
  @Mapping(target = "name", source = "soundRequestModel.name")
  @Mapping(target = "description", source = "soundRequestModel.description")
  @Mapping(target = "contentType", source = "file.contentType")
  @Mapping(target = "size", source = "file.size")
  @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
  @Mapping(target = "audioFile", source = "file.bytes")
  SoundEntity toEntity(SoundRequestModel soundRequestModel, MultipartFile file) throws IOException;

  GetSoundResponse toGetResponse(SoundEntity entity);

  SoundDTO toSoundDTO(SoundEntity entity);

  UserDTO toUserDTO(Users user);
}
