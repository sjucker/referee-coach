package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.Fixtures.gameDiscussion;
import static ch.stefanjucker.refereecoach.Fixtures.videoReport;
import static ch.stefanjucker.refereecoach.dto.Reportee.FIRST_REFEREE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.dto.OverviewDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

class SearchServiceTest extends AbstractIntegrationTest {

    @Autowired
    private SearchService searchService;

    @Test
    void search() {
    }

    @Test
    void findAll() {
        saveVideoReport("1", LocalDate.of(2023, 9, 21));
        saveVideoReport("2", LocalDate.of(2023, 9, 22));
        saveVideoReport("3", LocalDate.of(2023, 9, 23));

        saveGameDiscussion("4", LocalDate.of(2023, 9, 21));
        saveGameDiscussion("5", LocalDate.of(2023, 9, 22));
        saveGameDiscussion("6", LocalDate.of(2023, 9, 23));

        var result = searchService.findAll(LocalDate.of(2023, 9, 22), LocalDate.of(2023, 9, 30), coach1.getEmail());

        assertThat(result).hasSize(4)
                          .extracting(OverviewDTO::id)
                          .containsExactly("3", "6", "2", "5");
    }

    @Test
    void findAll_RefereeCoach() {
        saveVideoReport("1", LocalDate.of(2023, 9, 21), refereeCoach1);
        saveVideoReport("2", LocalDate.of(2023, 9, 22), refereeCoach1);
        saveVideoReport("3", LocalDate.of(2023, 9, 23), coach2);

        saveGameDiscussion("4", LocalDate.of(2023, 9, 21));
        saveGameDiscussion("5", LocalDate.of(2023, 9, 22));
        saveGameDiscussion("6", LocalDate.of(2023, 9, 23), refereeCoach1);

        var result = searchService.findAll(LocalDate.of(2023, 9, 22), LocalDate.of(2023, 9, 30), refereeCoach1.getEmail());

        assertThat(result).hasSize(2)
                          .extracting(OverviewDTO::id)
                          .containsExactly("6", "2");
    }

    @Test
    void findAll_Empty() {
        saveVideoReport("1", LocalDate.of(2023, 9, 21));
        saveVideoReport("2", LocalDate.of(2023, 9, 22));
        saveVideoReport("3", LocalDate.of(2023, 9, 23));

        saveGameDiscussion("4", LocalDate.of(2023, 9, 21));
        saveGameDiscussion("5", LocalDate.of(2023, 9, 22));
        saveGameDiscussion("6", LocalDate.of(2023, 9, 23));

        var result = searchService.findAll(LocalDate.of(2023, 9, 22), LocalDate.of(2023, 9, 30), referee5.getEmail());

        assertThat(result).isEmpty();
    }

    private void saveVideoReport(String id, LocalDate date) {
        saveVideoReport(id, date, coach1);
    }

    private void saveVideoReport(String id, LocalDate date, User coach) {
        videoReportRepository.save(videoReport(id, "", date, coach, referee1, referee2, referee3, FIRST_REFEREE));
    }

    private void saveGameDiscussion(String id, LocalDate date) {
        saveGameDiscussion(id, date, referee1);
    }

    private void saveGameDiscussion(String id, LocalDate date, User referee1) {
        gameDiscussionRepository.save(gameDiscussion(id, "1", date, referee1, referee2, referee3));
    }
}
