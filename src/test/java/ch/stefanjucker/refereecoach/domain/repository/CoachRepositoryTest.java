package ch.stefanjucker.refereecoach.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CoachRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CoachRepository coachRepository;

    @Test
    void findByEmail() {
        assertThat(coachRepository.findByEmail("test")).isEmpty();
    }

    @Test
    void findByEmail2() {
        assertThat(coachRepository.findByEmail("test")).isEmpty();
    }
}

