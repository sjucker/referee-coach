package ch.stefanjucker.refereecoach.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class UserRepositoryTest extends AbstractIntegrationTest {

    @Test
    void findByEmail() {
        assertThat(userRepository.findByEmail("test")).isEmpty();
        assertThat(userRepository.findByEmail(coach1.getEmail())).isNotEmpty();
    }

    @Test
    void findByName() {
        assertThat(userRepository.findByName(referee1.getName() + "!")).isEmpty();
        assertThat(userRepository.findByName(referee1.getName())).isNotEmpty();
    }

}
