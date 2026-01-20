package com.soundboard.soundboard.service;

import com.soundboard.soundboard.domain.AudioStorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LocalAudioStorageService {

  private final AudioStorageProperties audioStorageProperties;
  private final Path rootPath;
  
  
  public LocalAudioStorageService(AudioStorageProperties audioStorageProperties) {
    this.audioStorageProperties = audioStorageProperties;
    this.rootPath = Paths.get(audioStorageProperties.basePath());
  }
  
  public String storeAudioFile(InputStream inputStream, String originalName) throws IOException  {
    // TODO: Identify a better folder hierarchy, for now using by date
  
    LocalDate localDate = LocalDate.now();
    Path dateDirectory = rootPath.resolve(
            localDate.getYear() + File.separator +
                    String.format("%02d", localDate.getMonthValue()) + File.separator
            + String.format("%02d", localDate.getDayOfMonth())
    );
  
    Files.createDirectories(dateDirectory);
    
    String extension = getFileExtension(originalName);
    
    String storedName = UUID.randomUUID() + (extension.isEmpty() ? "" : "."+extension);
    
    Path filePath = dateDirectory.resolve(storedName);
    
    try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
      StreamUtils.copy(inputStream, outputStream);
    }
    
    // return relative path instead of absolute path
    return rootPath.relativize(filePath).toString();
  }
  
  public Resource getAudioResource(String storedPath) throws IOException {
    Path filePath = rootPath.resolve(storedPath).normalize().toAbsolutePath();
    Path normalizedRoot = rootPath.normalize().toAbsolutePath();
    
     // prevent file path manipulation from malicious users
    if (!filePath.startsWith(normalizedRoot)) {
      throw new SecurityException("Access Denied");
    }
    
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException("File does not exist");
    }
    
    return new UrlResource(filePath.toUri());
  }
  
  private String getFileExtension(String fileName) {
    int lastDot = fileName.lastIndexOf(".");
    return lastDot == -1 ? "" : fileName.substring(lastDot + 1);
  }
}
