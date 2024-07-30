package ch.stefanjucker.refereecoach;

import static ch.stefanjucker.refereecoach.Fixtures.coach;
import static ch.stefanjucker.refereecoach.Fixtures.referee;
import static ch.stefanjucker.refereecoach.Fixtures.refereeCoach;
import static org.mockito.Mockito.mock;

import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionRepository;
import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoCommentReplyRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoCommentRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoReportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final MySQLContainer MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.32"));

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected GameDiscussionRepository gameDiscussionRepository;
    @Autowired
    protected VideoReportRepository videoReportRepository;
    @Autowired
    protected VideoCommentRepository videoCommentRepository;
    @Autowired
    protected VideoCommentReplyRepository videoCommentReplyRepository;

    protected User coach1;
    protected User coach2;
    protected User refereeCoach1;
    protected User referee1;
    protected User referee2;
    protected User referee3;
    protected User referee4;
    protected User referee5;
    protected User referee6;
    protected User referee7;

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        MYSQL_CONTAINER.start();
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.driverClassName", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        public JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
    }

    @BeforeEach
    void setUp() {
        coach1 = userRepository.save(coach("Fabrizio Pizio"));
        coach2 = userRepository.save(coach("Caspar Schaudt"));
        refereeCoach1 = userRepository.save(refereeCoach("Novakovic Slobodan"));
        referee1 = userRepository.save(referee("Carr Ashley"));
        referee2 = userRepository.save(referee("Balletta Davide"));
        referee3 = userRepository.save(referee("Cid Prades Josep"));
        referee4 = userRepository.save(referee("Michaelides Markos"));
        referee5 = userRepository.save(referee("Demierre Martin"));
        referee6 = userRepository.save(referee("Stojcev Bosko"));
        referee7 = userRepository.save(referee("Vitalini Fabiano"));
    }

    @AfterEach
    void tearDown() {
        videoCommentReplyRepository.deleteAll();
        videoCommentRepository.deleteAll();
        videoReportRepository.deleteAll();
        gameDiscussionRepository.deleteAll();
        userRepository.deleteAll();
    }

}
