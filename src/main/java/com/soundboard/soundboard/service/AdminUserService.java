package com.soundboard.soundboard.service;

import com.soundboard.soundboard.config.AdminProperties;
import com.soundboard.soundboard.mapper.IMapper;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.UserDTO;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.requestModels.CreateAdminUserRequest;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.repository.SoundRepository;
import com.soundboard.soundboard.security.MyUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final MyUserRepo userRepo;
    private final SoundRepository soundRepository;
    private final LocalAudioStorageService audioStorageService;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;
    private final IMapper mapper;

    public AdminUserService(MyUserRepo userRepo,
                            SoundRepository soundRepository,
                            LocalAudioStorageService audioStorageService,
                            AdminProperties adminProperties,
                            PasswordEncoder passwordEncoder,
                            IMapper mapper) {
        this.userRepo = userRepo;
        this.soundRepository = soundRepository;
        this.audioStorageService = audioStorageService;
        this.adminProperties = adminProperties;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    // --- List ---

    @Transactional(readOnly = true)
    public List<UserDTO> listUsers(MyUserPrincipal caller) {
        List<Users> users = isAdmin(caller)
                ? userRepo.findAllByRole(Role.USER)
                : userRepo.findAll();
        return users.stream().map(mapper::toUserDTO).toList();
    }

    // --- Get single ---

    @Transactional(readOnly = true)
    public UserDTO getUser(UUID id, MyUserPrincipal caller) {
        Users target = findOrThrow(id);
        requireScopeAccess(caller, target);
        return mapper.toUserDTO(target);
    }

    // --- Toggle active ---

    @Transactional
    public UserDTO toggleActive(UUID id, MyUserPrincipal caller) {
        Users target = findOrThrow(id);
        requireScopeAccess(caller, target);
        requireNotSelf(caller, target, "toggle active status of");
        target.setActive(!target.isActive());
        userRepo.save(target);
        log.warn("AUDIT: {} '{}' toggled active={} on user '{}'",
                caller.getRole(), caller.getUsername(), target.isActive(), target.getUsername());
        return mapper.toUserDTO(target);
    }

    // --- Create (SUPER_ADMIN only) ---

    @Transactional
    public UserDTO createUser(CreateAdminUserRequest request, MyUserPrincipal caller) {
        if (userRepo.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        String displayName = (request.displayName() != null && !request.displayName().isBlank())
                ? request.displayName()
                : request.username();
        Users newUser = Users.builder()
                .username(request.username())
                .displayName(displayName)
                .password(passwordEncoder.encode(request.password()))
                .createdAt(Instant.now())
                .active(true)
                .role(request.role())
                .mustChangePassword(adminProperties.forcePasswordChange())
                .build();
        Users saved = userRepo.save(newUser);
        log.warn("AUDIT: SUPER_ADMIN '{}' created user '{}' with role={} mustChangePassword={}",
                caller.getUsername(), saved.getUsername(), saved.getRole(), saved.isMustChangePassword());
        return mapper.toUserDTO(saved);
    }

    // --- Hard delete (SUPER_ADMIN only) ---

    @Transactional
    public void hardDelete(UUID id, MyUserPrincipal caller) {
        Users target = findOrThrow(id);
        requireNotSelf(caller, target, "hard-delete");
        if (target.getRole() == Role.SUPER_ADMIN && userRepo.countByRole(Role.SUPER_ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot hard-delete the last SUPER_ADMIN account");
        }
        List<SoundEntity> sounds = soundRepository.findAllByOwnedBy(target.getUsername());
        soundRepository.deleteAll(sounds);
        userRepo.delete(target);

        // Best-effort disk cleanup after DB commit
        for (SoundEntity sound : sounds) {
            if (sound.getStoredName() != null && !sound.getStoredName().isBlank()) {
                try {
                    audioStorageService.deleteAudioFile(sound.getStoredName());
                } catch (IOException e) {
                    log.warn("AUDIT: Failed to delete disk file for sound {}: {}", sound.getId(), e.getMessage());
                }
            }
        }
        log.warn("AUDIT: SUPER_ADMIN '{}' hard-deleted user '{}' and {} associated sounds",
                caller.getUsername(), target.getUsername(), sounds.size());
    }

    // --- Helpers ---

    private Users findOrThrow(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean isAdmin(MyUserPrincipal caller) {
        return caller.getRole() == Role.ADMIN;
    }

    private void requireScopeAccess(MyUserPrincipal caller, Users target) {
        if (isAdmin(caller) && target.getRole() != Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "ADMIN role may only access USER-role accounts");
        }
    }

    private void requireNotSelf(MyUserPrincipal caller, Users target, String action) {
        if (caller.getUsername().equals(target.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot " + action + " your own account");
        }
    }
}
