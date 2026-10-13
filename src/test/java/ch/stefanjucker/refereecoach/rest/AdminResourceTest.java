package ch.stefanjucker.refereecoach.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AdminResourceTest extends AbstractIntegrationTest {

    @Autowired
    private AdminResource adminResource;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanAccessEndpoints() {
        authenticateWith("COACH", "ADMIN");

        assertThat(adminResource.getUsers()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"COACH", "REFEREE", "REFEREE_COACH"})
    void nonAdminIsDenied(String role) {
        authenticateWith(role);

        assertThatThrownBy(adminResource::getUsers).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> adminResource.deleteTag(1L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void anonymousIsDenied() {
        assertThatThrownBy(adminResource::getUsers).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private static void authenticateWith(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user@example.com", null, authorities));
    }
}
