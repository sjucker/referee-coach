package ch.stefanjucker.refereecoach.domain.repository;

import static ch.stefanjucker.refereecoach.domain.VideoReport.CURRENT_VERSION;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_3PO;
import static ch.stefanjucker.refereecoach.dto.Reportee.FIRST_REFEREE;
import static ch.stefanjucker.refereecoach.dto.Reportee.SECOND_REFEREE;
import static ch.stefanjucker.refereecoach.dto.Reportee.THIRD_REFEREE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.domain.BasketplanGame;
import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.CriteriaEvaluation;
import ch.stefanjucker.refereecoach.domain.VideoComment;
import ch.stefanjucker.refereecoach.domain.VideoReport;
import ch.stefanjucker.refereecoach.dto.Reportee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Set;

class VideoCommentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private RefereeRepository refereeRepository;
    @Autowired
    private VideoReportRepository videoReportRepository;
    @Autowired
    private VideoCommentRepository videoCommentRepository;

    @AfterEach
    void tearDown() {
        videoCommentRepository.deleteAll();
        videoReportRepository.deleteAll();
        coachRepository.deleteAll();
    }

    @Test
    void findVideoCommentsByGameNumberAndCoach() {
        // given
        var coach1 = coachRepository.save(createCoach("coach1"));
        var coach2 = coachRepository.save(createCoach("coach2"));

        var videoReport1 = videoReportRepository.save(createVideoReport("vr1", coach1, "g1", FIRST_REFEREE));
        var videoReport2 = videoReportRepository.save(createVideoReport("vr2", coach1, "g1", SECOND_REFEREE));
        var videoReport3 = videoReportRepository.save(createVideoReport("vr3", coach1, "g1", THIRD_REFEREE));

        var videoReport4 = videoReportRepository.save(createVideoReport("vr4", coach1, "g2", FIRST_REFEREE));
        var videoReport5 = videoReportRepository.save(createVideoReport("vr5", coach2, "g3", FIRST_REFEREE));

        videoCommentRepository.save(new VideoComment(null, 1, "Comment 1/1", videoReport1.getId(), false, Set.of()));
        videoCommentRepository.save(new VideoComment(null, 2, "Comment 2/1", videoReport1.getId(), false, Set.of()));

        videoCommentRepository.save(new VideoComment(null, 1, "Comment 1/2", videoReport2.getId(), false, Set.of()));
        videoCommentRepository.save(new VideoComment(null, 2, "Comment 2/2", videoReport2.getId(), false, Set.of()));

        videoCommentRepository.save(new VideoComment(null, 1, "Comment 1/3", videoReport3.getId(), false, Set.of()));
        videoCommentRepository.save(new VideoComment(null, 2, "Comment 2/3", videoReport3.getId(), false, Set.of()));

        videoCommentRepository.save(new VideoComment(null, 1, "Comment 1/4", videoReport4.getId(), false, Set.of()));
        videoCommentRepository.save(new VideoComment(null, 1, "Comment 1/5", videoReport5.getId(), false, Set.of()));

        // when
        var result = videoCommentRepository.findVideoCommentsByGameNumberAndCoach("g1", coach1.getId());

        // then
        assertThat(result).hasSize(2)
                          .satisfies(videoComment -> assertThat(videoComment.getComment()).isEqualTo("Comment 1/1"), atIndex(0));
    }

    private Coach createCoach(String email) {
        return new Coach(null, email, "Coach 1", "", false, null);
    }

    private BasketplanGame getBasketplanGame(String id) {
        var basketplanGame = new BasketplanGame();
        basketplanGame.setGameNumber(id);
        basketplanGame.setDate(LocalDate.now());
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

    private VideoReport createVideoReport(String id, Coach coach, String gameNumber, Reportee reportee) {
        return new VideoReport(id, coach, reportee, getBasketplanGame(gameNumber),
                               new CriteriaEvaluation(), new CriteriaEvaluation(), new CriteriaEvaluation(), new CriteriaEvaluation(),
                               new CriteriaEvaluation(), new CriteriaEvaluation(), new CriteriaEvaluation(), null, null, false, null, CURRENT_VERSION);
    }

}

