package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.Fixtures.videoComment;
import static ch.stefanjucker.refereecoach.Fixtures.videoReport;
import static ch.stefanjucker.refereecoach.dto.Reportee.FIRST_REFEREE;
import static ch.stefanjucker.refereecoach.dto.Reportee.SECOND_REFEREE;
import static ch.stefanjucker.refereecoach.dto.Reportee.THIRD_REFEREE;
import static ch.stefanjucker.refereecoach.dto.UserRole.REFEREE_COACH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.dto.BasketplanGameDTO;
import ch.stefanjucker.refereecoach.dto.Reportee;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

class VideoReportServiceTest extends AbstractIntegrationTest {

    @Autowired
    private VideoReportService videoReportService;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private BasketplanService basketplanService;

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

    @Test
    void createRejectsSelfReport() {
        // given a referee-coach that is part of the referee crew of the game they want to coach
        var refereeCoach = userRepository.findByName(referee2.getName()).orElseThrow();
        refereeCoach.setRole(REFEREE_COACH);
        var coach = userRepository.save(refereeCoach);
        var game = basketplanService.findGameByNumber("22-00249").orElseThrow();
        var ownReportee = reporteeOf(game, coach.getId());

        // when / then
        assertThatThrownBy(() -> videoReportService.create(game.gameNumber(), game.youtubeId(), ownReportee, coach))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not allowed to create a report about themselves");
        assertThat(videoReportRepository.findAll()).isEmpty();

        // coaching one of the other referees of the same game is still allowed
        var otherReportee = Arrays.stream(Reportee.values()).filter(r -> r != ownReportee).findFirst().orElseThrow();
        assertThat(videoReportService.create(game.gameNumber(), game.youtubeId(), otherReportee, coach)).isNotNull();
        assertThat(videoReportRepository.findAll()).hasSize(1);
    }

    @Test
    void copyRejectsSelfReport() {
        // given a report of a referee-coach who is themselves the second referee of that game
        videoReportRepository.save(videoReport("1", refereeCoach1, referee1, refereeCoach1, referee3, FIRST_REFEREE));

        // when / then
        assertThatThrownBy(() -> videoReportService.copy("1", SECOND_REFEREE, refereeCoach1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not allowed to create a report about themselves");
        assertThat(videoReportRepository.findAll()).hasSize(1);

        // copying to one of the other referees of the same game is still allowed
        assertThat(videoReportService.copy("1", THIRD_REFEREE, refereeCoach1)).isNotNull();
        assertThat(videoReportRepository.findAll()).hasSize(2);
    }

    private static Reportee reporteeOf(BasketplanGameDTO game, Long refereeId) {
        if (game.referee1() != null && game.referee1().id().equals(refereeId)) {
            return FIRST_REFEREE;
        } else if (game.referee2() != null && game.referee2().id().equals(refereeId)) {
            return SECOND_REFEREE;
        } else if (game.referee3() != null && game.referee3().id().equals(refereeId)) {
            return THIRD_REFEREE;
        }
        throw new IllegalArgumentException("referee %s is not part of the crew of %s".formatted(refereeId, game.gameNumber()));
    }
}
