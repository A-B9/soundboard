package com.soundboard.soundboard.unit.service;

import com.soundboard.soundboard.audio.AudioStorageProperties;
import com.soundboard.soundboard.exceptions.SoundNotFoundException;
import com.soundboard.soundboard.mapper.IMapper;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.sound.GetSoundResponse;
import com.soundboard.soundboard.repository.SoundRepository;
import com.soundboard.soundboard.service.LocalAudioStorageService;
import com.soundboard.soundboard.service.SoundService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestSoundService {
  
  @Mock // create a mockito mock of the dependency
  private SoundRepository soundRepository;
  
  @Mock // every method returns null/empty values by default, but you can specify return values for specific method calls using Mockito's when() and thenReturn() methods.
  private LocalAudioStorageService storageService;
  
  @Mock
  private AudioStorageProperties properties;
  
  @Mock
  private IMapper mapper;
  
  @InjectMocks // creates a real instance of the class and injects mocks into it automatically.
  private SoundService soundService;
  
  // --- CREATE ---
  @Test
  void testCreate_Success() throws IOException {
    // Arrange
    // Set up the necessary data and mock behavior for the test
    SoundRequestModel requestModel = mock(SoundRequestModel.class);
    MultipartFile file = mock(MultipartFile.class);
    String username = "testUser";
    SoundEntity soundEntity = new SoundEntity();
    
    when(mapper.toEntity(requestModel, file)).thenReturn(soundEntity);
    when(file.isEmpty()).thenReturn(false);
    when(file.getContentType()).thenReturn("audio/wav");
    when(file.getOriginalFilename()).thenReturn("testSound.wav");
    when(file.getInputStream()).thenReturn(mock(InputStream.class));
    when(storageService.storeAudioFile(any(), any())).thenReturn("stored/file/path");
    when(properties.allowedMimeTypes()).thenReturn(Set.of("audio/wav", "audio/mpeg"));
    
    // Act
    // Call the method being tested
    soundService.create(requestModel, file, username);
    
    // Assert
    // Verify the expected outcomes, such as interactions with mocks or returned values
    verify(soundRepository, times(1)).save(soundEntity);
    assertThat(soundEntity.getOwnedBy()).isEqualTo(username);
  }
  
  @Test
  void testCreate_InvalidMimeType() {
  
  }
  
  @Test
  void testCreate_EmptyFile() {
  
  }
  
  // --- DELETE ---

  @Test
  void testDelete_Success() throws IOException {
    Long soundId = 1L;
    String username = "testUser";
    SoundEntity entity = SoundEntity.builder().storedName("path/to/file.wav").build();

    when(soundRepository.findByIdAndOwnedBy(soundId, username)).thenReturn(Optional.of(entity));

    soundService.delete(soundId, username);

    verify(storageService, times(1)).deleteAudioFile("path/to/file.wav");
    verify(soundRepository, times(1)).delete(entity);
  }

  @Test
  void testDelete_NotOwner_throwsSoundNotFoundException() {
    Long soundId = 1L;
    String username = "otherUser";

    when(soundRepository.findByIdAndOwnedBy(soundId, username)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> soundService.delete(soundId, username))
            .isInstanceOf(SoundNotFoundException.class);
  }
  
  // --- GET ALL ---
  @Test
  @Disabled("Test is currently disabled due to issues with mocking Pageable and Page. Needs further investigation.")
  void testGetAll_Success() {
    // arrange
    String username = "testUser";
    Pageable pageable = mock(Pageable.class);
    
    Page<GetSoundResponse> expected = mock(Page.class);
    
    Page<SoundEntity> page = mock(Page.class);
    
//    when(soundRepository.findAllByOwnedTo(any(), any())).thenReturn(page);
//    when(mapper.toGetResponse(any())).thenReturn(expected);
//
//    Page<ResponseBodyModel> result = soundService.getAll(pageable, username);
//
//      // assert
//    assertThat(result).isNotNull();
//    assertThat(result).isEqualTo(expected);
  }
  
  // --- GET BY ID ---
  
  @Test
  void testGetById_Success() {
    // arrange
    Long id = 1L;
    String username = "testUser";
    SoundEntity soundEntity = new SoundEntity();
    GetSoundResponse expected = mock(GetSoundResponse.class);
    
    when(soundRepository.findByIdAndOwnedBy(any(), any())).thenReturn(Optional.of(soundEntity));
    when(mapper.toGetResponse(soundEntity)).thenReturn(expected);
    
    // act
    GetSoundResponse result = soundService.getById(id, username);
    
    // assert
    assertThat(result).isEqualTo(expected);
    
  }
  
  @Test
  void testGetById_NotFound() {
  
  }
  
  @Test
  void testGetById_Unauthorized() {
  
  }
  
  // --- GET AUDIO FILE ---
  @Test
  void testGetAudioFile_Success() throws IOException {
    // arrange
    Long id = 1L;
    String username = "testUser";
    SoundEntity soundEntity = SoundEntity.builder()
            .ownedBy(username)
            .storedName("storedName")
            .build();
    Resource expected = mock(Resource.class);
    when(soundRepository.findByIdAndOwnedBy(any(), any())).thenReturn(Optional.of(soundEntity));
    when(storageService.getAudioResource(any())).thenReturn(expected);
    
    // act
    Resource result = soundService.getAudioFile(id, username);
    
    // assert
    verify(storageService, times(1)).getAudioResource(any());
    assertThat(result).isEqualTo(expected);
    
  }
  
  @Test
  void testUploadAudio_Success() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    SoundEntity soundEntity = new SoundEntity();
    InputStream inputStream = mock(InputStream.class);
    
    when(file.isEmpty()).thenReturn(false);
    when(file.getContentType()).thenReturn("audio/mp3");
    when(file.getInputStream()).thenReturn(inputStream);
    when(storageService.storeAudioFile(any(), any())).thenReturn("stored/file/path");
    when(properties.allowedMimeTypes()).thenReturn(Set.of("audio/wav", "audio/mp3"));
    
    soundService.uploadAudio(file, soundEntity);
    
    assertThat(soundEntity.getStoredName()).isEqualTo("stored/file/path");
  }
}
