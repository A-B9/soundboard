package com.soundboard.soundboard.unit.service;

import com.soundboard.soundboard.audio.AudioStorageProperties;
import com.soundboard.soundboard.service.LocalAudioStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link LocalAudioStorageService}. Uses a JUnit {@link TempDir}
 * so file I/O is real but isolated to a per-test directory.
 */
class TestLocalAudioStorageService {

    @TempDir
    Path tempDir;

    private LocalAudioStorageService storageService;
    private AudioStorageProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AudioStorageProperties(
                tempDir.toString(),
                Set.of("audio/mp3", "audio/wav", "audio/wave")
        );
        storageService = new LocalAudioStorageService(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        // TempDir cleanup is automatic, but normalize any nested writes for safety
        if (Files.exists(tempDir)) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder())
                        .filter(p -> !p.equals(tempDir))
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                                // best-effort cleanup
                            }
                        });
            }
        }
    }

    // --- storeAudioFile ---

    @Test
    void storeAudioFile_writesFileUnderDateHierarchy_andReturnsRelativePath() throws IOException {
        byte[] payload = "RIFF....fakeaudiopayload".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(payload);

        String relativePath = storageService.storeAudioFile(inputStream, "clip.wav");

        // Path is relative (does not start with tempDir absolute prefix)
        assertThat(relativePath).isNotBlank();
        Path stored = tempDir.resolve(relativePath);
        assertThat(Files.exists(stored)).isTrue();
        assertThat(Files.readAllBytes(stored)).isEqualTo(payload);

        // Path uses today's date directory structure: yyyy/MM/dd
        LocalDate today = LocalDate.now();
        String expectedPrefix = String.format("%d%s%02d%s%02d",
                today.getYear(), java.io.File.separator,
                today.getMonthValue(), java.io.File.separator,
                today.getDayOfMonth());
        assertThat(relativePath).startsWith(expectedPrefix);

        // The filename has the extension preserved
        assertThat(relativePath).endsWith(".wav");
    }

    @Test
    void storeAudioFile_withNoExtensionInOriginalName_storesFileWithoutExtension() throws IOException {
        byte[] payload = "noextpayload".getBytes();
        String relativePath = storageService.storeAudioFile(new ByteArrayInputStream(payload), "noextfile");

        Path stored = tempDir.resolve(relativePath);
        assertThat(Files.exists(stored)).isTrue();

        // Name is a UUID with no extension — last segment must NOT contain '.'
        String fileName = stored.getFileName().toString();
        assertThat(fileName).doesNotContain(".");
    }

    @Test
    void storeAudioFile_multipleCalls_produceDifferentStoredNames() throws IOException {
        String firstPath = storageService.storeAudioFile(new ByteArrayInputStream("a".getBytes()), "clip.mp3");
        String secondPath = storageService.storeAudioFile(new ByteArrayInputStream("b".getBytes()), "clip.mp3");

        assertThat(firstPath).isNotEqualTo(secondPath);
        assertThat(Files.exists(tempDir.resolve(firstPath))).isTrue();
        assertThat(Files.exists(tempDir.resolve(secondPath))).isTrue();
    }

    // --- getAudioResource ---

    @Test
    void getAudioResource_returnsUrlResource_whenFileExists() throws IOException {
        byte[] payload = "audiocontents".getBytes();
        String relativePath = storageService.storeAudioFile(new ByteArrayInputStream(payload), "clip.mp3");

        Resource resource = storageService.getAudioResource(relativePath);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        try (var in = resource.getInputStream()) {
            assertThat(in.readAllBytes()).isEqualTo(payload);
        }
    }

    @Test
    void getAudioResource_throwsFileNotFound_whenFileMissing() {
        assertThatThrownBy(() -> storageService.getAudioResource("does/not/exist.wav"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("File does not exist");
    }

    @Test
    void getAudioResource_throwsSecurityException_onPathTraversal() {
        assertThatThrownBy(() -> storageService.getAudioResource("../../etc/passwd"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access Denied");
    }

    // --- deleteAudioFile ---

    @Test
    void deleteAudioFile_removesFileFromDisk_whenItExists() throws IOException {
        byte[] payload = "to-be-deleted".getBytes();
        String relativePath = storageService.storeAudioFile(new ByteArrayInputStream(payload), "delete.wav");
        Path stored = tempDir.resolve(relativePath);
        assertThat(Files.exists(stored)).isTrue();

        storageService.deleteAudioFile(relativePath);

        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void deleteAudioFile_silentlyDoesNothing_whenFileMissing() {
        // Should NOT throw — implementation uses Files.deleteIfExists
        Path nonExistent = tempDir.resolve("missing.wav");
        assertThat(Files.exists(nonExistent)).isFalse();

        try {
            storageService.deleteAudioFile("missing.wav");
        } catch (IOException e) {
            org.junit.jupiter.api.Assertions.fail("deleteAudioFile should not throw when file is absent");
        }
    }

    @Test
    void deleteAudioFile_throwsSecurityException_onPathTraversal() {
        assertThatThrownBy(() -> storageService.deleteAudioFile("../../sensitive-file.txt"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access Denied");
    }
}
