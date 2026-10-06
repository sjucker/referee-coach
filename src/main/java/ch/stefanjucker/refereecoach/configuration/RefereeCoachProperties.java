package ch.stefanjucker.refereecoach.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "referee.coach")
public class RefereeCoachProperties {
    private String jwtSecret;
    private String baseUrl;
    private boolean overrideRecipient;
    private String ccMail;
    private String bccMail;
    private String overrideRecipientMail;
    private char[] impersonationPassword;
    /**
     * Additional origins (besides the one of {@link #baseUrl}) from which passkeys may be used, e.g. the herokuapp.com hostname.
     */
    private List<String> passkeyExtraOrigins = new ArrayList<>();
}
