package ch.stefanjucker.refereecoach.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
}
