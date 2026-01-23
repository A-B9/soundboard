package com.soundboard.soundboard.service;

import com.soundboard.soundboard.models.requestModels.SoundRequestModel;
import com.soundboard.soundboard.models.responseModels.ResponseBodyModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface IService {
  
  public void create(SoundRequestModel soundRequestModel, MultipartFile file) throws IOException;
  public void delete(Long id);
  public Page<ResponseBodyModel> getAll(Pageable pageable);
  public ResponseBodyModel getById(Long id);
  
}
