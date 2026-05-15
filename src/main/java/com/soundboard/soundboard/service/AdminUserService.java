package com.soundboard.soundboard.service;

import com.soundboard.soundboard.audit.AuditAction;
import com.soundboard.soundboard.audit.AuditLogger;
import com.soundboard.soundboard.config.AdminProperties;
import com.soundboard.soundboard.mapper.IMapper;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.UserDTO;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.requestModels.CreateAdminUserRequest;
import com.soundboard.soundboard.models.requestModels.PatchUserRequest;
import com.soundboard.soundboard.repository.MyUserRepo;
import com.soundboard.soundboard.repository.SoundRepository;
import com.soundboard.soundboard.security.MyUserPrincipal;
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

    private final MyUserRepo userRepo;
    private final SoundRepository soundRepository;
    private final LocalAudioStorageService audioStorageService;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;
    private final IMapper mapper;
    private final AuditLogger auditLogger;

    public AdminUserService(MyUserRepo userRepo,
                            SoundRepository soundRepository,
                            LocalAudioStorageService audioStorageService,
                            AdminProperties adminProperties,
                            PasswordEncoder passwordEncoder,
                            IMapper mapper,
                            AuditLogger auditLogger) {
        this.userRepo = userRepo;
        this.soundRepository = soundRepository;
        this.audioStorageService = audioStorageService;
        this.adminProperties = adminProperties;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.auditLogger = auditLogger;
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
        auditLogger.log(AuditAction.USER_ACTIVE_TOGGLED, caller,
                "active=%s for user '%s'".formatted(target.isActive(), target.getUsername()));
        return mapper.toUserDTO(target);
    }
    
    @Transactional
    public UserDTO patchUser(UUID id, PatchUserRequest request, MyUserPrincipal caller) {
        Users target = findOrThrow(id);
        requireNotSelf(caller, target, "patch");
        checkIfAbleToPatch(caller, target);
        requireScopeAccess(caller, target);
        
        if (target.isMustChangePassword() == request.mustChangePassword()) {
            return mapper.toUserDTO(target);
        }
        
        target.setMustChangePassword(request.mustChangePassword());
        userRepo.save(target);
        auditLogger.log(AuditAction.USER_MUST_CHANGE_PASSWORD_SET, caller,
                "mustChangePassword=%s for user '%s'".formatted(request.mustChangePassword(), target.getUsername()));
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
        auditLogger.log(AuditAction.USER_CREATED, caller,
                "created user '%s' role=%s mustChangePassword=%s"
                        .formatted(saved.getUsername(), saved.getRole(), saved.isMustChangePassword()));
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
                    auditLogger.warn(AuditAction.DISK_FILE_DELETE_FAILED,
                            "soundId=%s error=%s".formatted(sound.getId(), e.getMessage()));
                }
            }
        }
        auditLogger.log(AuditAction.USER_HARD_DELETED, caller,
                "deleted user '%s' and %d sounds".formatted(target.getUsername(), sounds.size()));
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
    
    private void checkIfAbleToPatch(MyUserPrincipal caller, Users target) {
        if (caller.getRole()  != Role.SUPER_ADMIN && target.getRole() != Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only SUPER_ADMIN may patch ADMIN accounts");
        }
    }
}
