package ch.stefanjucker.refereecoach.security;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserAuthService userAuthService;

    public JwtRequestFilter(JwtService jwtService, UserAuthService userAuthService) {
        this.jwtService = jwtService;
        this.userAuthService = userAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);

        var jwtToken = authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX) ? authorizationHeader.substring(BEARER_PREFIX.length()).strip() : "";
        if (!jwtToken.isEmpty()) {
            Optional<Claims> jwt = jwtService.validateJwt(jwtToken);
            if (jwt.isPresent()) {
                var email = jwt.get().getSubject();
                try {
                    var userDetails = userAuthService.loadUserByUsername(email);
                    var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UsernameNotFoundException e) {
                    // just continue unauthenticated if username unknown (e.g, email needed changing) => user gets logged out
                    logger.warn("could not authenticate user, since username was unknown", e);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
