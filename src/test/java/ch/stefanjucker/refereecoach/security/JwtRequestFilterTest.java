package ch.stefanjucker.refereecoach.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtRequestFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtRequestFilter filter = new JwtRequestFilter(jwtService, mock(UserAuthService.class));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer", "Bearer ", "Bearer    ", "BearerXYZ", "Basic abc"})
    void malformedAuthorizationHeaderContinuesUnauthenticated(String header) throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, header);
        var chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).validateJwt(any());
    }
}
