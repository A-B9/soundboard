package com.soundboard.soundboard.unit.mapper;

import com.soundboard.soundboard.mapper.IMapper;
import com.soundboard.soundboard.mapper.IMapperImpl;
import com.soundboard.soundboard.models.SoundDTO;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.sound.GetSoundResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {IMapperImpl.class})
class TestMapper {
  
  @Autowired
  private IMapper mapper;
  
  // -- to SoundDTO --
  
  @Test
  void testToEntity() throws IOException {
    SoundRequestModel dto = new SoundRequestModel(
            "Test Sound",
            "A sound for testing"
    );
  
    MultipartFile file = mock(MultipartFile.class);
    when(file.getContentType()).thenReturn(".wav");
    when(file.getSize()).thenReturn(1024L);
    SoundEntity expected = new SoundEntity(
            "Test Sound",
            "A sound for testing",
            ".wav",
            null,
            Instant.now(),
            null,
            null,
            1024L
    );
    expected.setId(1L);
    
    
    SoundEntity actual = mapper.toEntity(dto, file);
    
//    assertThat(actual.getId()).isEqualTo(expected.getId());
    assertThat(actual.getName()).isEqualTo(expected.getName());
    assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
    assertThat(actual.getContentType()).isEqualTo(expected.getContentType());
    assertThat(actual.getSize()).isEqualTo(expected.getSize());
    assertThat(actual.getStoredName()).isEqualTo(expected.getStoredName());
    assertThat(actual.getOwnedTo()).isEqualTo(expected.getOwnedTo());
    
  }
  
  @Test
  void testToSoundDTO() {
    SoundEntity entity = new SoundEntity(
            "Test Sound Entity",
            "A sound for testing",
            ".mp3",
            null,
            Instant.now(),
            "This is the stored name",
            "this wont move either",
            1024L
    );
    entity.setId(1L);
    
    SoundDTO expected = new SoundDTO(
            1L,
            "Test Sound Entity",
            "A sound for testing",
            entity.getCreatedAt(),
            "This is the stored name",
            1024L
    );
    
    SoundDTO actual = mapper.toSoundDTO(entity);
    
    assertThat(actual.id()).isEqualTo(expected.id());
    assertThat(actual.name()).isEqualTo(expected.name());
    assertThat(actual.description()).isEqualTo(expected.description());
    assertThat(actual.createdAt()).isEqualTo(expected.createdAt());
    assertThat(actual.storedName()).isEqualTo(expected.storedName());
    assertThat(actual.size()).isEqualTo(expected.size());
  }
  
  @Test
  void testToGetResponse() {
    SoundEntity entity = new SoundEntity(
            "Test GetSoundResponse",
            "A sound for testing",
            ".mp3",
            null,
            Instant.now(),
            "This is the stored name",
            "this wont move either",
            1024L
    );
    entity.setId(1L);
    
    GetSoundResponse expected = new GetSoundResponse(
            1L,
            "Test GetSoundResponse",
            "A sound for testing",
            ".mp3",
            1024L
    );
    
    GetSoundResponse actual = mapper.toGetResponse(entity);
    
    assertThat(actual.id()).isEqualTo(expected.id());
    assertThat(actual.name()).isEqualTo(expected.name());
    assertThat(actual.description()).isEqualTo(expected.description());
    assertThat(actual.contentType()).isEqualTo(expected.contentType());
    assertThat(actual.size()).isEqualTo(expected.size());
  }
  
}
