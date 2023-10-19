package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.Fixtures.COACH_EMAIL;
import static ch.stefanjucker.refereecoach.Fixtures.coach;
import static ch.stefanjucker.refereecoach.Fixtures.referee;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_3PO;
import static ch.stefanjucker.refereecoach.dto.Reportee.FIRST_REFEREE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.domain.BasketplanGame;
import ch.stefanjucker.refereecoach.domain.GameDiscussion;
import ch.stefanjucker.refereecoach.domain.VideoReport;
import ch.stefanjucker.refereecoach.domain.repository.CoachRepository;
import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionRepository;
import ch.stefanjucker.refereecoach.domain.repository.RefereeRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoReportRepository;
import ch.stefanjucker.refereecoach.dto.OverviewDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

class SearchServiceTest extends AbstractIntegrationTest {

    @Autowired
    private VideoReportRepository videoReportRepository;
    @Autowired
    private GameDiscussionRepository gameDiscussionRepository;
    @Autowired
    private RefereeRepository refereeRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        coachRepository.save(coach());
        refereeRepository.save(referee("Carr Ashley"));
        refereeRepository.save(referee("Balletta Davide"));
        refereeRepository.save(referee("Cid Prades Josep"));
        refereeRepository.save(referee("Some Referee"));
    }

    @AfterEach
    void tearDown() {
        videoReportRepository.deleteAll();
        gameDiscussionRepository.deleteAll();
        coachRepository.deleteAll();
        refereeRepository.deleteAll();
    }

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

        var result = searchService.findAll(LocalDate.of(2023, 9, 22), LocalDate.of(2023, 9, 30), COACH_EMAIL);

        assertThat(result).hasSize(4)
                          .extracting(OverviewDTO::id)
                          .containsExactly("3", "6", "2", "5");
    }

    @Test
    void findAll_Empty() {
        saveVideoReport("1", LocalDate.of(2023, 9, 21));
        saveVideoReport("2", LocalDate.of(2023, 9, 22));
        saveVideoReport("3", LocalDate.of(2023, 9, 23));

        saveGameDiscussion("4", LocalDate.of(2023, 9, 21));
        saveGameDiscussion("5", LocalDate.of(2023, 9, 22));
        saveGameDiscussion("6", LocalDate.of(2023, 9, 23));

        var result = searchService.findAll(LocalDate.of(2023, 9, 22), LocalDate.of(2023, 9, 30), "Some Referee");

        assertThat(result).isEmpty();
    }

    private void saveVideoReport(String id, LocalDate date) {
        var videoReport = new VideoReport();
        videoReport.setId(id);
        videoReport.setCoach(coachRepository.findByEmail(COACH_EMAIL).orElseThrow());
        videoReport.setReportee(FIRST_REFEREE);
        var basketplanGame = getBasketplanGame(id, date);
        videoReport.setBasketplanGame(basketplanGame);

        videoReportRepository.save(videoReport);
    }

    private BasketplanGame getBasketplanGame(String id, LocalDate date) {
        var basketplanGame = new BasketplanGame();
        basketplanGame.setGameNumber(id);
        basketplanGame.setDate(date);
        basketplanGame.setCompetition("");
        basketplanGame.setResult("");
        basketplanGame.setTeamA("");
        basketplanGame.setTeamB("");
        basketplanGame.setOfficiatingMode(OFFICIATING_3PO);
        basketplanGame.setYoutubeId("");
        basketplanGame.setReferee1(refereeRepository.findByName("Balletta Davide").orElseThrow());
        basketplanGame.setReferee2(refereeRepository.findByName("Carr Ashley").orElseThrow());
        basketplanGame.setReferee3(refereeRepository.findByName("Cid Prades Josep").orElseThrow());
        return basketplanGame;
    }

    private void saveGameDiscussion(String id, LocalDate date) {
        var gameDiscussion = new GameDiscussion();
        gameDiscussion.setId(id);
        gameDiscussion.setBasketplanGame(getBasketplanGame(id, date));
        gameDiscussionRepository.save(gameDiscussion);
    }
}
