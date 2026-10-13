package ch.stefanjucker.refereecoach.security;

import static io.jsonwebtoken.Jwts.SIG.HS256;
import static java.nio.charset.StandardCharsets.UTF_8;

import ch.stefanjucker.refereecoach.configuration.RefereeCoachProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Optional;

@Slf4j
@Service
public class JwtService {

    private static final MacAlgorithm SIGNATURE_ALGORITHM = HS256;
    private final SecretKey secretKey;

    public JwtService(RefereeCoachProperties properties) {
        // explicit HmacSHA256 key: Keys.hmacShaKeyFor() would pick the algorithm by key length
        this.secretKey = new SecretKeySpec(properties.getJwtSecret().getBytes(UTF_8), "HmacSHA256");
    }

    public String createJwt(String email) {
        return Jwts.builder()
                   .subject(email)
                   .signWith(secretKey, SIGNATURE_ALGORITHM)
                   .compact();
    }

    public Optional<Claims> validateJwt(String jwt) {
        var jwtParser = Jwts.parser()
                            .verifyWith(secretKey)
                            .build();

        try {
            return Optional.of(jwtParser.parseSignedClaims(jwt).getPayload());
        } catch (JwtException ex) {
            log.error("unable to parse given JWT: " + jwt, ex);
            return Optional.empty();
        }
    }

}
