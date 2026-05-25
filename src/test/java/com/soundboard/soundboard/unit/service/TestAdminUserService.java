package com.soundboard.soundboard.unit.service;

import com.soundboard.soundboard.audit.AuditLogger;
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
import com.soundboard.soundboard.service.AdminUserService;
import com.soundboard.soundboard.service.LocalAudioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestAdminUserService {

    @Mock private MyUserRepo userRepo;
    @Mock private SoundRepository soundRepository;
    @Mock private LocalAudioStorageService audioStorageService;
    @Mock private AdminProperties adminProperties;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IMapper mapper;
    @Mock private AuditLogger auditLogger;

    private AdminUserService service;

    @BeforeEach
    void setUp() {
        service = new AdminUserService(userRepo, soundRepository, audioStorageService,
                adminProperties, passwordEncoder, mapper, auditLogger);
    }

    // --- listUsers ---

    @Test
    void listUsers_adminCaller_returnsOnlyUserRoleAccounts() {
        MyUserPrincipal admin = principalWithRole("admin", Role.ADMIN);
        Users user1 = user("alice", Role.USER);
        when(userRepo.findAllByRole(Role.USER)).thenReturn(List.of(user1));
        when(mapper.toUserDTO(user1)).thenReturn(dto(user1));

        List<UserDTO> result = service.listUsers(admin);

        assertThat(result).hasSize(1);
        verify(userRepo).findAllByRole(Role.USER);
        verify(userRepo, never()).findAll();
    }

    @Test
    void listUsers_superAdminCaller_returnsAllUsers() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        Users user1 = user("alice", Role.USER);
        Users admin1 = user("bob", Role.ADMIN);
        when(userRepo.findAll()).thenReturn(List.of(user1, admin1));
        when(mapper.toUserDTO(any())).thenAnswer(i -> dto(i.getArgument(0)));

        List<UserDTO> result = service.listUsers(sa);

        assertThat(result).hasSize(2);
        verify(userRepo).findAll();
        verify(userRepo, never()).findAllByRole(any());
    }

    // --- getUser ---

    @Test
    void getUser_adminCaller_canAccessUserRoleAccount() {
        MyUserPrincipal admin = principalWithRole("admin", Role.ADMIN);
        Users target = user("alice", Role.USER);
        UUID id = target.getId();
        when(userRepo.findById(id)).thenReturn(Optional.of(target));
        when(mapper.toUserDTO(target)).thenReturn(dto(target));

        UserDTO result = service.getUser(id, admin);

        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    void getUser_adminCaller_throwsForbidden_whenTargetIsAdmin() {
        MyUserPrincipal admin = principalWithRole("admin", Role.ADMIN);
        Users target = user("other-admin", Role.ADMIN);
        when(userRepo.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.getUser(target.getId(), admin))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getUser_adminCaller_throwsForbidden_whenTargetIsSuperAdmin() {
        MyUserPrincipal admin = principalWithRole("admin", Role.ADMIN);
        Users target = user("superadmin", Role.SUPER_ADMIN);
        when(userRepo.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.getUser(target.getId(), admin))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getUser_superAdminCaller_canAccessAdminRoleAccount() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        Users target = user("other-admin", Role.ADMIN);
        when(userRepo.findById(target.getId())).thenReturn(Optional.of(target));
        when(mapper.toUserDTO(target)).thenReturn(dto(target));

        UserDTO result = service.getUser(target.getId(), sa);

        assertThat(result.username()).isEqualTo("other-admin");
    }

    @Test
    void getUser_throwsNotFound_whenUserDoesNotExist() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        UUID missing = UUID.randomUUID();
        when(userRepo.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(missing, sa))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- toggleActive ---

    @Test
    void toggleActive_flipsActiveFlag_fromTrueToFalse() {
        MyUserPrincipal admin = principalWithRole("admin", Role.ADMIN);
        Users target = userWithActive("alice", Role.USER, true);
        when(userRepo.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepo.save(target)).thenReturn(target);
        when(mapper.toUserDTO(target)).thenReturn(dto(target));

        service.toggleActive(target.getId(), admin);

        assertThat(target.isActive()).isFalse();
    }

    @Test
    void toggleActive_flipsActiveFlag_fromFalseToTrue() {
        MyUserPrincipal admin = principalWithRole("admin", Role.ADMIN);
        Users target = userWithActive("alice", Role.USER, false);
        when(userRepo.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepo.save(target)).thenReturn(target);
        when(mapper.toUserDTO(target)).thenReturn(dto(target));

        service.toggleActive(target.getId(), admin);

        assertThat(target.isActive()).isTrue();
    }

    @Test
    void toggleActive_adminCaller_throwsForbidden_whenTargetIsAdmin() {
        MyUserPrincipal admin = principalWithRole("admin", Role.ADMIN);
        Users target = user("other-admin", Role.ADMIN);
        when(userRepo.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.toggleActive(target.getId(), admin))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void toggleActive_superAdminCaller_throwsConflict_whenTargetIsSelf() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        Users self = user("superadmin", Role.SUPER_ADMIN);
        when(userRepo.findById(self.getId())).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.toggleActive(self.getId(), sa))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    // --- createUser ---

    @Test
    void createUser_savesUserWithCorrectRole() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        CreateAdminUserRequest request = new CreateAdminUserRequest(
                "newadmin", "SecurePass123!", Role.ADMIN, null);
        when(userRepo.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("hashed");
        when(adminProperties.forcePasswordChange()).thenReturn(true);
        when(userRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toUserDTO(any())).thenAnswer(i -> dto(i.getArgument(0)));

        service.createUser(request, sa);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(captor.getValue().isMustChangePassword()).isTrue();
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
    }

    @Test
    void createUser_throwsConflict_whenUsernameAlreadyExists() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        CreateAdminUserRequest request = new CreateAdminUserRequest(
                "existing", "SecurePass123!", Role.USER, null);
        when(userRepo.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request, sa))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createUser_usesUsername_asDisplayName_whenDisplayNameIsNull() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        CreateAdminUserRequest request = new CreateAdminUserRequest(
                "newuser", "SecurePass123!", Role.USER, null);
        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(adminProperties.forcePasswordChange()).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toUserDTO(any())).thenAnswer(i -> dto(i.getArgument(0)));

        service.createUser(request, sa);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getDisplayName()).isEqualTo("newuser");
    }

    // --- hardDelete ---

    @Test
    void hardDelete_deletesUserAndSoundsFromDb() throws Exception {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        Users target = user("alice", Role.USER);
        SoundEntity sound = sound(target.getUsername());
        when(userRepo.findById(target.getId())).thenReturn(Optional.of(target));
        when(soundRepository.findAllByOwnedBy(target.getUsername())).thenReturn(List.of(sound));

        service.hardDelete(target.getId(), sa);

        verify(soundRepository).deleteAll(List.of(sound));
        verify(userRepo).delete(target);
    }

    @Test
    void hardDelete_throwsConflict_whenTargetIsSelf() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        Users self = user("superadmin", Role.SUPER_ADMIN);
        when(userRepo.findById(self.getId())).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.hardDelete(self.getId(), sa))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void hardDelete_throwsConflict_whenTargetIsLastSuperAdmin() {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        Users otherSa = user("other-superadmin", Role.SUPER_ADMIN);
        when(userRepo.findById(otherSa.getId())).thenReturn(Optional.of(otherSa));
        when(userRepo.countByRole(Role.SUPER_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.hardDelete(otherSa.getId(), sa))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void hardDelete_allowsDeletion_whenMultipleSuperAdminsExist() throws Exception {
        MyUserPrincipal sa = principalWithRole("superadmin", Role.SUPER_ADMIN);
        Users otherSa = user("other-superadmin", Role.SUPER_ADMIN);
        when(userRepo.findById(otherSa.getId())).thenReturn(Optional.of(otherSa));
        when(userRepo.countByRole(Role.SUPER_ADMIN)).thenReturn(2L);
        when(soundRepository.findAllByOwnedBy(otherSa.getUsername())).thenReturn(List.of());

        service.hardDelete(otherSa.getId(), sa);

        verify(userRepo).delete(otherSa);
    }

    // --- Builder helpers ---

    private MyUserPrincipal principalWithRole(String username, Role role) {
        Users u = Users.builder()
                .username(username).role(role).active(true).build();
        return new MyUserPrincipal(u);
    }

    private Users user(String username, Role role) {
        return Users.builder()
                .username(username).role(role).active(true)
                .createdAt(Instant.now()).build();
    }

    private Users userWithActive(String username, Role role, boolean active) {
        Users u = Users.builder()
                .username(username).role(role).active(active)
                .createdAt(Instant.now()).build();
        return u;
    }

    private SoundEntity sound(String ownedBy) {
        return SoundEntity.builder()
                .name("test-sound").description("desc")
                .contentType("audio/wav").audioFile(new byte[]{1})
                .createdAt(Instant.now()).storedName("2024/01/01/test.wav")
                .ownedBy(ownedBy).size(1L).build();
    }

    private UserDTO dto(Users u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getDisplayName(), u.getCreatedAt(), u.getRole(), u.isActive());
    }
}
