package ch.stefanjucker;

import ch.stefanjucker.configuration.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Import(TestConfig.class)
//@Testcontainers
public abstract class AbstractIntegrationTest {

    //    @Container
    static final MySQLContainer MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.31"));

    static {
        MYSQL_CONTAINER.start();
    }

    // TODO stop at end?

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.driverClassName", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

}
