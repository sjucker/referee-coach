package ch.stefanjucker.refereecoach.configuration;

import ch.stefanjucker.refereecoach.security.PasskeyCredentialRepositoryAdapter;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.HashSet;

@Configuration
public class PasskeyConfiguration {

    /**
     * Relying party ID and origin are derived from the base URL (e.g., https://app.referee-coach.ch).
     */
    @Bean
    public RelyingParty relyingParty(RefereeCoachProperties properties,
                                     PasskeyCredentialRepositoryAdapter credentialRepository) {
        var baseUrl = URI.create(properties.getBaseUrl());

        var origins = new HashSet<String>();
        origins.add(origin(baseUrl));
        origins.addAll(properties.getPasskeyExtraOrigins());

        return RelyingParty.builder()
                           .identity(RelyingPartyIdentity.builder()
                                                         .id(baseUrl.getHost())
                                                         .name("Referee Coach")
                                                         .build())
                           .credentialRepository(credentialRepository)
                           .origins(origins)
                           .build();
    }

    private static String origin(URI uri) {
        return uri.getPort() == -1 ?
                "%s://%s".formatted(uri.getScheme(), uri.getHost()) :
                "%s://%s:%d".formatted(uri.getScheme(), uri.getHost(), uri.getPort());
    }
}
