package ch.stefanjucker.refereecoach.configuration;

import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfiguration {

    /**
     * Tomcat rejects requests whose Accept-Language header lists more than 10 locales (default since 11.0.27) with an
     * IllegalArgumentException, which surfaces as a 500. Browsers with many configured languages can exceed that.
     */
    private static final int MAX_LOCALE_COUNT = 100;

    @Bean
    public TomcatConnectorCustomizer maxLocaleCountCustomizer() {
        return connector -> connector.setMaxLocaleCount(MAX_LOCALE_COUNT);
    }
}
