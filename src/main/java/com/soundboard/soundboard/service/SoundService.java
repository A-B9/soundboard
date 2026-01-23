package com.soundboard.soundboard.service;

import com.soundboard.soundboard.models.SoundDTO;
import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.ResponseBodyModel;
import com.soundboard.soundboard.mapper.Mapper;
import com.soundboard.soundboard.repository.SoundRepository;
import com.soundboard.soundboard.audio.AudioStorageProperties;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.responseModels.GetSoundResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
@Service // Indicates that this class is a service component in Spring, making it eligible for component scanning and dependency injection
// Service classes typically contain business logic and interact with repositories to manage data
public class SoundService implements IService{
    
    LocalAudioStorageService storageService;
    AudioStorageProperties properties;

    private final SoundRepository soundRepository;
    private final Mapper mapper;

    @Autowired
    public SoundService(SoundRepository soundRepository,
                        LocalAudioStorageService storageService,
                        AudioStorageProperties properties,
                        Mapper mapper) {
        this.soundRepository = soundRepository;
        this.properties = properties;
        this.storageService = storageService;
        this.mapper = mapper;
    }
    
    public void create(SoundRequestModel soundRequest, MultipartFile file) throws IOException {
        try {
            SoundEntity soundEntity = mapper.toEntity(soundRequest, file);
            uploadAudio(file, soundEntity);
            soundRepository.save(soundEntity);
        } catch (Exception e) {
            throw new IOException("Audio could not be stored");
        }
    }
    
    public void delete(Long id) {
        soundRepository.deleteById(id);
    }

    public Page<ResponseBodyModel> getAll(Pageable pageable) {
        return soundRepository.findAll(pageable).map(mapper::toGetResponse);
    }

    public GetSoundResponse getById(Long id) {
        return soundRepository.findById(id).map(mapper::toGetResponse)
                .orElseThrow(() -> new IllegalArgumentException("Sound not found with id: " + id));
    }
    
    public Resource getAudioFile(Long id) throws IOException {
        SoundDTO dto = soundRepository.findById(id).map(mapper::toSoundDTO).orElseThrow();
        return storageService.getAudioResource(dto.storedName());
    }
    
    public void uploadAudio(MultipartFile file, SoundEntity sound) throws IOException {
        validateAudio(file);
        String storagePath;
        
        try (InputStream inputStream = file.getInputStream()) {
            storagePath = storageService.storeAudioFile(inputStream, file.getOriginalFilename());
        }
        sound.setStoredName(storagePath);
        
    }
    
    private void validateAudio(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty, please pass in a file.");
        }
        
        String mimeType = file.getContentType();
        if (mimeType.equals(null) || !properties.allowedMimeTypes().contains(mimeType)) {
            throw new IllegalArgumentException("Invalid content-type for the provided file.");
        }
    }
    
}
