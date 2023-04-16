package ch.stefanjucker.refereecoach.domain.repository;

import static ch.stefanjucker.refereecoach.domain.Referee.RefereeLevel.GROUP_2;
import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.domain.Referee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RefereeRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private RefereeRepository refereeRepository;

    @BeforeEach
    void setUp() {
        var referee = new Referee();
        referee.setName("Test Ref");
        referee.setEmail("test@ref.com");
        referee.setLevel(GROUP_2);
        refereeRepository.save(referee);
    }

    @AfterEach
    void tearDown() {
        refereeRepository.deleteAll();
    }

    @Test
    void findByName() {
        assertThat(refereeRepository.findByName("Test Reef")).isEmpty();
        assertThat(refereeRepository.findByName("Test Ref")).isNotEmpty();
    }
}
