package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.Fixtures.videoComment;
import static ch.stefanjucker.refereecoach.Fixtures.videoReport;
import static ch.stefanjucker.refereecoach.dto.Reportee.SECOND_REFEREE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalDateTime;

class VideoReportServiceTest extends AbstractIntegrationTest {

    @Autowired
    private VideoReportService videoReportService;
    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    void sendReminderEmails() {
        // given
        var videoReport = videoReport("1", coach1, referee1, referee2, referee3, SECOND_REFEREE);
        videoReport.setFinished(true);
        videoReport.setFinishedAt(LocalDateTime.now().minusDays(2).minusHours(1));
        videoReport.setReminderSent(false);
        videoReportRepository.save(videoReport);
        videoCommentRepository.save(videoComment("1", true));

        // when
        videoReportService.sendReminderEmails();

        // then
        assertThat(videoReportRepository.findById("1")).hasValueSatisfying(vr -> assertThat(vr.isReminderSent()).isTrue());

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, atLeastOnce()).send(captor.capture());
        assertThat(captor.getValue()).satisfies(simpleMailMessage -> {
            assertThat(simpleMailMessage.getTo()).contains(referee2.getEmail());
            assertThat(simpleMailMessage.getSubject()).isEqualTo("[Referee Coach] Reminder Required Replies");
            assertThat(simpleMailMessage.getText()).isEqualToIgnoringNewLines(
                    """
                            Hi Balletta Davide
                            
                            This is a reminder that you have not yet replied to all important comments.
                            Please visit: https://app.referee-coach.ch/#/discuss/1
                            """);
        });
    }

    @Test
    void updateMissingScores() {
        // given
        var videoReport = videoReport("1", "22-00249", LocalDate.of(2022, 10, 1), coach1, referee1, referee2, referee3, SECOND_REFEREE);
        videoReport.setFinished(true);
        videoReport.getBasketplanGame().setResult("? - ?");
        videoReportRepository.save(videoReport);

        // when
        videoReportService.updateMissingScores();

        // then
        assertThat(videoReportRepository.findById("1")).hasValueSatisfying(vr -> assertThat(vr.getBasketplanGame().getResult()).isEqualTo("82 - 98"));
    }
}
