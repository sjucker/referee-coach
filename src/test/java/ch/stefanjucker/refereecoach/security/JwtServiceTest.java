package ch.stefanjucker.refereecoach.security;

import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.configuration.RefereeCoachProperties;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

    private static final String SECRET = "verysecurejwtsecretfortesting1234567890?!";
    // issued with jjwt 0.11.5 and SECRET: tokens of logged-in users must survive library upgrades
    private static final String LEGACY_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjb2FjaEBleGFtcGxlLmNvbSJ9.Un8t7PMkzQKo_UDrIEmvr7AA-bSzGziSrYV0LAj_Vgg";

    private final JwtService jwtService = jwtService(SECRET);

    @Test
    void createdJwtCanBeValidated() {
        var jwt = jwtService.createJwt("coach@example.com");

        assertThat(jwtService.validateJwt(jwt)).map(Claims::getSubject).hasValue("coach@example.com");
    }

    @Test
    void legacyJwtIsStillValid() {
        assertThat(jwtService.validateJwt(LEGACY_JWT)).map(Claims::getSubject).hasValue("coach@example.com");
    }

    @Test
    void createdJwtIsUnchanged() {
        assertThat(jwtService.createJwt("coach@example.com")).as("same token as jjwt 0.11.5").isEqualTo(LEGACY_JWT);
    }

    @Test
    void jwtSignedWithOtherSecretIsRejected() {
        var jwt = jwtService(SECRET + "other").createJwt("coach@example.com");

        assertThat(jwtService.validateJwt(jwt)).isEmpty();
    }

    @Test
    void unsignedJwtIsRejected() {
        var unsigned = LEGACY_JWT.substring(0, LEGACY_JWT.lastIndexOf('.') + 1).replace("eyJhbGciOiJIUzI1NiJ9", "eyJhbGciOiJub25lIn0");

        assertThat(jwtService.validateJwt(unsigned)).isEmpty();
    }

    private static JwtService jwtService(String secret) {
        var properties = new RefereeCoachProperties();
        properties.setJwtSecret(secret);
        return new JwtService(properties);
    }
}
