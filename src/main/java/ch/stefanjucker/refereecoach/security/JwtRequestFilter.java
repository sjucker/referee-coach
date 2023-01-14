package ch.stefanjucker.refereecoach.security;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
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

    private final JwtService jwtService;
    private final UserAuthService userAuthService;

    public JwtRequestFilter(JwtService jwtService, UserAuthService userAuthService) {
        this.jwtService = jwtService;
        this.userAuthService = userAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith("Bearer")) {
            var jwtToken = authorizationHeader.substring(7);
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
                    filterChain.doFilter(request, response);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
