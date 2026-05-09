package com.soundboard.soundboard.service;

import com.soundboard.soundboard.exceptions.SoundNotFoundException;
import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.sound.ResponseBodyModel;
import com.soundboard.soundboard.mapper.IMapper;
import com.soundboard.soundboard.repository.SoundRepository;
import com.soundboard.soundboard.audio.AudioStorageProperties;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.responseModels.sound.GetSoundResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class SoundService {

    LocalAudioStorageService storageService;
    AudioStorageProperties properties;

    private final SoundRepository soundRepository;
    private final IMapper mapper;

    @Autowired
    public SoundService(SoundRepository soundRepository,
                        LocalAudioStorageService storageService,
                        AudioStorageProperties properties,
                        IMapper mapper) {
        this.soundRepository = soundRepository;
        this.properties = properties;
        this.storageService = storageService;
        this.mapper = mapper;
    }

    public void create(SoundRequestModel soundRequest,
                       MultipartFile file,
                       String username) throws IOException {
        try {

            SoundEntity soundEntity = mapper.toEntity(soundRequest, file);
            soundEntity.setOwnedBy(username);
            uploadAudio(file, soundEntity);
            soundRepository.save(soundEntity);

        } catch (Exception e) {
            throw new IOException("Audio could not be stored");
        }
    }

    @Transactional
    public void delete(Long id, String username) throws IOException {
        SoundEntity entity = soundRepository.findByIdAndOwnedBy(id, username)
                .orElseThrow(() -> new SoundNotFoundException(id));
        storageService.deleteAudioFile(entity.getStoredName());
        soundRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Page<ResponseBodyModel> getAll(Pageable pageable, String username) {
        return soundRepository.findAllByOwnedBy(pageable, username).map(mapper::toGetResponse);
    }

    @Transactional(readOnly = true)
    public GetSoundResponse getById(Long id, String username) {
        return soundRepository.findByIdAndOwnedBy(id, username).map(mapper::toGetResponse)
                .orElseThrow(() -> new SoundNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Resource getAudioFile(Long id, String username) throws IOException {
        SoundEntity entity = soundRepository.findByIdAndOwnedBy(id, username)
                .orElseThrow(() -> new SoundNotFoundException(id));
        return storageService.getAudioResource(entity.getStoredName());
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
        if (mimeType == null || !properties.allowedMimeTypes().contains(mimeType)) {
            throw new IllegalArgumentException("Invalid content-type for the provided file.");
        }
    }

    public List<GetSoundResponse> searchSound(String keyword, String username) {
        return soundRepository.searchByOwner(keyword, username).stream().map(mapper::toGetResponse).toList();
    }

}
