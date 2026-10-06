package ch.stefanjucker.refereecoach.scheduled;

import static java.util.concurrent.TimeUnit.MINUTES;

import ch.stefanjucker.refereecoach.service.PasskeyService;
import ch.stefanjucker.refereecoach.service.VideoReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledTasks {

    private final VideoReportService videoReportService;
    private final PasskeyService passkeyService;

    public ScheduledTasks(VideoReportService videoReportService, PasskeyService passkeyService) {
        this.videoReportService = videoReportService;
        this.passkeyService = passkeyService;
    }

    @Scheduled(initialDelay = 1, fixedRate = 60, timeUnit = MINUTES)
    public void sendReminderEmail() {
        log.info("checking for video report that are missing required replies");
        videoReportService.sendReminderEmails();
    }

    @Scheduled(initialDelay = 0, fixedRate = 60, timeUnit = MINUTES)
    public void updateMissingScores() {
        log.info("checking for video report that are missing final score");
        videoReportService.updateMissingScores();
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Zurich")
    public void deleteExpiredPasskeyCeremonies() {
        log.info("deleted {} expired passkey ceremonies", passkeyService.deleteExpiredCeremonies());
    }
}
