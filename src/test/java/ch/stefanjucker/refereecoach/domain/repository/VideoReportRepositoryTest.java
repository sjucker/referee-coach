package ch.stefanjucker.refereecoach.domain.repository;

import static ch.stefanjucker.refereecoach.Fixtures.videoComment;
import static ch.stefanjucker.refereecoach.Fixtures.videoCommentReply;
import static ch.stefanjucker.refereecoach.Fixtures.videoReport;
import static ch.stefanjucker.refereecoach.dto.Reportee.FIRST_REFEREE;
import static ch.stefanjucker.refereecoach.dto.Reportee.SECOND_REFEREE;
import static ch.stefanjucker.refereecoach.dto.Reportee.THIRD_REFEREE;
import static ch.stefanjucker.refereecoach.util.DateUtil.now;
import static org.assertj.core.api.Assertions.assertThat;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.Referee;
import ch.stefanjucker.refereecoach.domain.VideoReport;
import ch.stefanjucker.refereecoach.dto.Reportee;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class VideoReportRepositoryTest extends AbstractIntegrationTest {

    @Test
    void findByBasketplanGameGameNumberAndCoachId() {
    }

    @Test
    void findAll() {
    }

    @Test
    void findReportIdsWithRequiredReplies() {
        // given
        videoReportRepository.save(createVideoReport("1", coach1, referee1, referee2, referee3, FIRST_REFEREE, true, now().minusDays(2).minusHours(1), false));
        videoReportRepository.save(createVideoReport("2", coach1, referee1, referee2, referee3, SECOND_REFEREE, true, now().minusDays(1), false));
        videoReportRepository.save(createVideoReport("3", coach1, referee1, referee2, referee3, THIRD_REFEREE, false, null, false));
        videoReportRepository.save(createVideoReport("4", coach2, referee2, referee3, null, FIRST_REFEREE, true, now().minusDays(3), true));
        videoReportRepository.save(createVideoReport("5", coach2, referee2, referee3, null, SECOND_REFEREE, true, now().minusDays(3), false));

        var videoComment1 = videoCommentRepository.save(videoComment("1", true)).getId();
        videoCommentReplyRepository.save(videoCommentReply(videoComment1, referee1.getName())); // required referee replied
        videoCommentRepository.save(videoComment("1", false)); // not required

        videoCommentRepository.save(videoComment("2", true)); // not yet 2 days ago
        videoCommentRepository.save(videoComment("3", true)); // not yet finished
        videoCommentRepository.save(videoComment("4", true)); // already reminded

        var videoComment3 = videoCommentRepository.save(videoComment("5", true)).getId();
        videoCommentReplyRepository.save(videoCommentReply(videoComment3, referee2.getName())); // only "other" referee replied
        videoCommentRepository.save(videoComment("5", true)); // make sure multiple comments are only registered once for report
        videoCommentRepository.save(videoComment("5", true));

        // when
        var result = videoReportRepository.findReportIdsWithRequiredReplies(now().minusDays(2));

        // then
        assertThat(result).hasSize(2).containsOnly("1", "5");
    }

    private VideoReport createVideoReport(String id, Coach coach, Referee referee1, Referee referee2, Referee referee3, Reportee reportee,
                                          boolean finished, LocalDateTime finishedAt, boolean reminderSent) {
        var videoReport = videoReport(id, coach, referee1, referee2, referee3, reportee);
        videoReport.setFinished(finished);
        videoReport.setFinishedAt(finishedAt);
        videoReport.setReminderSent(reminderSent);
        return videoReport;
    }
}
