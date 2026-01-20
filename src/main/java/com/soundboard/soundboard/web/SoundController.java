package com.soundboard.soundboard.web;

import com.soundboard.soundboard.domain.models.AudioFileDTO;
import com.soundboard.soundboard.domain.models.SoundEntity;
import com.soundboard.soundboard.domain.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.domain.models.responseModels.CreateSoundResponse;
import com.soundboard.soundboard.domain.models.responseModels.GetSoundResponse;
import com.soundboard.soundboard.service.SoundService;
import org.springframework.core.io.Resource;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<CreateSoundResponse> createSound(@RequestPart("soundRequest") SoundRequestModel soundRequest,
                                                           @RequestPart("file") MultipartFile file) throws IOException {
        
        soundService.createSound(soundRequest, file);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(new CreateSoundResponse(
                        soundRequest.name(),
                        soundRequest.description()
                ));
    }
    
    @GetMapping("/sounds")
    @ResponseBody
    public ResponseEntity<List<GetSoundResponse>> getAllSounds() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(soundService.getAllSounds()
                );
    }
    
    @GetMapping("/sounds/{id}")
    public ResponseEntity<GetSoundResponse> getSound(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(soundService.getSoundById(id)
                );
    }
    
    @DeleteMapping("/sounds/{id}")
    public ResponseEntity<SoundEntity> deleteSound(@PathVariable Long id) {
        soundService.deleteSound(id);
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
