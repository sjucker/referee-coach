package ch.stefanjucker.refereecoach.domain.repository;

import static ch.stefanjucker.refereecoach.Fixtures.coach;
import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CoachRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CoachRepository coachRepository;

    @BeforeEach
    void setUp() {
        coachRepository.save(coach());
    }

    @AfterEach
    void tearDown() {
        coachRepository.deleteAll();
    }

    @Test
    void findByEmail() {
        assertThat(coachRepository.findByEmail("test")).isEmpty();
        assertThat(coachRepository.findByEmail("coach@referee-coach.ch")).isNotEmpty();
    }

}
