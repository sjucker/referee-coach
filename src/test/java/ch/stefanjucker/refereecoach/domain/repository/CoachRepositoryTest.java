package ch.stefanjucker.refereecoach.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class CoachRepositoryTest extends AbstractIntegrationTest {

    @Test
    void findByEmail() {
        assertThat(coachRepository.findByEmail("test")).isEmpty();
        assertThat(coachRepository.findByEmail(coach1.getEmail())).isNotEmpty();
    }

}
