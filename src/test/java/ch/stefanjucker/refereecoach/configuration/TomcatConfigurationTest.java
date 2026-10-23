package ch.stefanjucker.refereecoach.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TomcatConfigurationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Test
    void acceptsRequestWithManyLocales() throws Exception {
        var acceptLanguage = Arrays.stream(Locale.getISOLanguages())
                                    .limit(20)
                                    .collect(Collectors.joining(","));

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:%d/api/video-report".formatted(port)))
                                     .header("Accept-Language", acceptLanguage)
                                     .build();

            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertThat(response.statusCode()).isNotEqualTo(500);
        }
    }
}
