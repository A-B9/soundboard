package com.soundboard.soundboard.unit.security;

import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.security.MyUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit tests for MyUserPrincipal.
 *
 * No Spring context is needed — MyUserPrincipal has no Spring dependencies.
 * Users is built via its Lombok @Builder so that @Builder.Default values for
 * role (Role.USER) and mustChangePassword (false) are applied correctly.
 */
class TestMyUserPrincipal {

    // --- getAuthorities() ---

    @Test
    void getAuthorities_returnsRoleUser_forUserRole() {
        Users user = Users.builder()
                .username("alice")
                .role(Role.USER)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void getAuthorities_returnsRoleAdmin_forAdminRole() {
        Users user = Users.builder()
                .username("bob")
                .role(Role.ADMIN)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void getAuthorities_returnsRoleSuperAdmin_forSuperAdminRole() {
        Users user = Users.builder()
                .username("carol")
                .role(Role.SUPER_ADMIN)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_SUPER_ADMIN");
    }

    // --- getRole() ---

    @Test
    void getRole_returnsCorrectRole() {
        Users user = Users.builder()
                .username("dave")
                .role(Role.ADMIN)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        assertThat(principal.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void getRole_returnsUserRole_whenBuilderDefaultApplied() {
        // Confirm that omitting .role() from the builder yields Role.USER via @Builder.Default
        Users user = Users.builder()
                .username("eve")
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        assertThat(principal.getRole()).isEqualTo(Role.USER);
    }

    // --- isMustChangePassword() ---

    @Test
    void isMustChangePassword_returnsTrue_whenSet() {
        Users user = Users.builder()
                .username("frank")
                .role(Role.USER)
                .mustChangePassword(true)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        assertThat(principal.isMustChangePassword()).isTrue();
    }

    @Test
    void isMustChangePassword_returnsFalse_byDefault() {
        // @Builder.Default sets mustChangePassword = false when not explicitly provided
        Users user = Users.builder()
                .username("grace")
                .role(Role.USER)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        assertThat(principal.isMustChangePassword()).isFalse();
    }

    @Test
    void isMustChangePassword_returnsFalse_whenExplicitlySetFalse() {
        Users user = Users.builder()
                .username("hank")
                .role(Role.ADMIN)
                .mustChangePassword(false)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        assertThat(principal.isMustChangePassword()).isFalse();
    }

    // --- getUsername() ---

    @Test
    void getUsername_returnsCorrectUsername() {
        Users user = Users.builder()
                .username("ivan")
                .role(Role.USER)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        assertThat(principal.getUsername()).isEqualTo("ivan");
    }

    // --- Authority format guard ---

    @Test
    void getAuthorities_doesNotContainRolePrefix_twice() {
        // Ensure the authority is "ROLE_ADMIN" and never accidentally "ROLE_ROLE_ADMIN"
        Users user = Users.builder()
                .username("judy")
                .role(Role.ADMIN)
                .build();
        MyUserPrincipal principal = new MyUserPrincipal(user);

        String authority = principal.getAuthorities().iterator().next().getAuthority();

        assertThat(authority).doesNotContain("ROLE_ROLE_");
        assertThat(authority).isEqualTo("ROLE_ADMIN");
    }
}
