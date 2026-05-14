package com.soundboard.soundboard.web;

import com.soundboard.soundboard.models.UserDTO;
import com.soundboard.soundboard.models.requestModels.CreateAdminUserRequest;
import com.soundboard.soundboard.models.requestModels.PatchUserRequest;
import com.soundboard.soundboard.security.MyUserPrincipal;
import com.soundboard.soundboard.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/soundboard/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserDTO>> listUsers(@AuthenticationPrincipal MyUserPrincipal caller) {
        return ResponseEntity.ok(adminUserService.listUsers(caller));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id,
                                           @AuthenticationPrincipal MyUserPrincipal caller) {
        return ResponseEntity.ok(adminUserService.getUser(id, caller));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserDTO> toggleActive(@PathVariable UUID id,
                                                @AuthenticationPrincipal MyUserPrincipal caller) {
        return ResponseEntity.ok(adminUserService.toggleActive(id, caller));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateAdminUserRequest request,
                                              @AuthenticationPrincipal MyUserPrincipal caller) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(request, caller));
    }
    
    @PatchMapping("/{id}/password-reset")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id,
                                              @Valid @RequestBody PatchUserRequest request,
                                              @AuthenticationPrincipal MyUserPrincipal caller) {
        
        return ResponseEntity.ok().body(
                adminUserService.patchUser(id,  request, caller)
        );
    }

    @DeleteMapping("/{id}/hard-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID id,
                                           @AuthenticationPrincipal MyUserPrincipal caller) {
        adminUserService.hardDelete(id, caller);
        return ResponseEntity.noContent().build();
    }
}
