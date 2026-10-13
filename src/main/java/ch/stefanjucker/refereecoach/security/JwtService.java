package ch.stefanjucker.refereecoach.security;

import static io.jsonwebtoken.Jwts.SIG.HS256;
import static java.lang.Byte.SIZE;
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
        var secret = properties.getJwtSecret().getBytes(UTF_8);
        if (secret.length * SIZE < SIGNATURE_ALGORITHM.getKeyBitLength()) {
            throw new IllegalStateException("JWT secret must be at least %d bytes long".formatted(SIGNATURE_ALGORITHM.getKeyBitLength() / SIZE));
        }
        // explicit HmacSHA256 key: Keys.hmacShaKeyFor() would pick the algorithm by key length
        this.secretKey = new SecretKeySpec(secret, "HmacSHA256");
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
        } catch (JwtException | IllegalArgumentException ex) {
            // never log the token itself: it is a valid credential if only slightly malformed
            log.warn("unable to parse given JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }

}
