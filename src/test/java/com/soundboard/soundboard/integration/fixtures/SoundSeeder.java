package com.soundboard.soundboard.integration.fixtures;

import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.repository.SoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SoundSeeder {

    private final SoundRepository soundRepository;
    private final MyUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SoundSeeder(SoundRepository soundRepository,
                       MyUserRepo userRepo,
                       PasswordEncoder passwordEncoder) {
        this.soundRepository = soundRepository;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Users seedUser(String username) {
        Users user = Users.builder()
                .username(username)
                .password(passwordEncoder.encode("test-password"))
                .createdAt(Instant.now())
                .active(true)
                .build();
        return userRepo.save(user);
    }

    public Users seedUserWithPassword(String username, String rawPassword) {
        Users user = Users.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .createdAt(Instant.now())
                .active(true)
                .build();
        return userRepo.save(user);
    }

    public SoundEntity seedSound(String name, String ownedBy) {
        SoundEntity sound = SoundEntity.builder()
                .name(name)
                .description("Test description for " + name)
                .contentType("audio/wav")
                .audioFile(new byte[]{1, 2, 3})
                .createdAt(Instant.now())
                .storedName(name.toLowerCase() + ".wav")
                .ownedBy(ownedBy)
                .size(3L)
                .build();
        return soundRepository.save(sound);
    }

    public Users seedAdmin(String username) {
        Users user = Users.builder()
                .username(username)
                .password(passwordEncoder.encode("test-password"))
                .createdAt(Instant.now())
                .active(true)
                .role(com.soundboard.soundboard.models.Role.ADMIN)
                .build();
        return userRepo.save(user);
    }

    public Users seedSuperAdmin(String username) {
        Users user = Users.builder()
                .username(username)
                .password(passwordEncoder.encode("test-password"))
                .createdAt(Instant.now())
                .active(true)
                .role(com.soundboard.soundboard.models.Role.SUPER_ADMIN)
                .build();
        return userRepo.save(user);
    }

    public void clearAll() {
        soundRepository.deleteAll();
        userRepo.deleteAll();
    }

    public void clearSounds() {
        soundRepository.deleteAll();
    }
}
