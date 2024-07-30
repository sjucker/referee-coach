package ch.stefanjucker.refereecoach;

import static ch.stefanjucker.refereecoach.Fixtures.coach;
import static ch.stefanjucker.refereecoach.Fixtures.referee;
import static org.mockito.Mockito.mock;

import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.Referee;
import ch.stefanjucker.refereecoach.domain.repository.CoachRepository;
import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionRepository;
import ch.stefanjucker.refereecoach.domain.repository.RefereeRepository;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3"));

    @Autowired
    protected CoachRepository coachRepository;
    @Autowired
    protected RefereeRepository refereeRepository;
    @Autowired
    protected GameDiscussionRepository gameDiscussionRepository;
    @Autowired
    protected VideoReportRepository videoReportRepository;
    @Autowired
    protected VideoCommentRepository videoCommentRepository;
    @Autowired
    protected VideoCommentReplyRepository videoCommentReplyRepository;

    protected Coach coach1;
    protected Coach coach2;
    protected Referee referee1;
    protected Referee referee2;
    protected Referee referee3;
    protected Referee referee4;
    protected Referee referee5;
    protected Referee referee6;
    protected Referee referee7;

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        POSTGRES_CONTAINER.start();
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.driverClassName", POSTGRES_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
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
        coach1 = coachRepository.save(coach("Fabrizio Pizio"));
        coach2 = coachRepository.save(coach("Caspar Schaudt"));
        referee1 = refereeRepository.save(referee("Carr Ashley"));
        referee2 = refereeRepository.save(referee("Balletta Davide"));
        referee3 = refereeRepository.save(referee("Cid Prades Josep"));
        referee4 = refereeRepository.save(referee("Michaelides Markos"));
        referee5 = refereeRepository.save(referee("Demierre Martin"));
        referee6 = refereeRepository.save(referee("Stojcev Bosko"));
        referee7 = refereeRepository.save(referee("Vitalini Fabiano"));
    }

    @AfterEach
    void tearDown() {
        videoCommentReplyRepository.deleteAll();
        videoCommentRepository.deleteAll();
        videoReportRepository.deleteAll();
        gameDiscussionRepository.deleteAll();
        coachRepository.deleteAll();
        refereeRepository.deleteAll();
    }

}
