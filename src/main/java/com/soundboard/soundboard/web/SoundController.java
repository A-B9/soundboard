package com.soundboard.soundboard.web;

import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.responseModels.CreateSoundResponse;
import com.soundboard.soundboard.models.responseModels.GetSoundResponse;
import com.soundboard.soundboard.models.responseModels.ResponseBodyModel;
import com.soundboard.soundboard.service.SoundService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@RestController
@CrossOrigin //CORS
@RequestMapping("/api/soundboard")
public class SoundController {
    
    private final SoundService soundService;

    public SoundController(SoundService soundService) {
        // Constructor injection
        this.soundService = soundService;
    }
    
    @PostMapping(value = "/sounds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateSoundResponse> createSound(@Valid @RequestPart("soundRequest") SoundRequestModel soundRequest,
                                                           @RequestPart("file") MultipartFile file) throws IOException {
        
        soundService.create(soundRequest, file);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(new CreateSoundResponse(
                        soundRequest.name(),
                        soundRequest.description()
                ));
    }
    
    @GetMapping("/sounds")
    @ResponseBody
    public ResponseEntity<Page<ResponseBodyModel>> getAllSounds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.status(HttpStatus.OK)
                .body(soundService.getAll(pageable)
                );
    }
    
    @GetMapping("/sounds/{id}")
    public ResponseEntity<GetSoundResponse> getSound(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(soundService.getById(id)
                );
    }
    
    @DeleteMapping("/sounds/{id}")
    public ResponseEntity<SoundEntity> deleteSound(@PathVariable Long id) {
        soundService.delete(id);
        return ResponseEntity.accepted()
                .body(null);
    }
    
    @GetMapping("/sounds/{id}/download")
    public ResponseEntity<Resource> getAudioFile(
            @PathVariable Long id
    ) throws IOException {
        
        try {
            Resource audioResource = soundService.getAudioFile(id);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + audioResource.getFilename() + "\"")
                    .body(audioResource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
