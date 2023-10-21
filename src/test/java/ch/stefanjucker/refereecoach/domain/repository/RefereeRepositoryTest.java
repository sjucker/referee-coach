package ch.stefanjucker.refereecoach.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class RefereeRepositoryTest extends AbstractIntegrationTest {

    @Test
    void findByName() {
        assertThat(refereeRepository.findByName(referee1.getName() + "!")).isEmpty();
        assertThat(refereeRepository.findByName(referee1.getName())).isNotEmpty();
    }
}
