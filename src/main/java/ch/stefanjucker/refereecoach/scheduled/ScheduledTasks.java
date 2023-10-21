package ch.stefanjucker.refereecoach.scheduled;

import static java.util.concurrent.TimeUnit.MINUTES;

import ch.stefanjucker.refereecoach.service.VideoReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledTasks {

    private final VideoReportService videoReportService;

    public ScheduledTasks(VideoReportService videoReportService) {
        this.videoReportService = videoReportService;
    }

    @Scheduled(initialDelay = 1, fixedRate = 60, timeUnit = MINUTES)
    public void sendReminderEmail() {
        log.info("checking for video report that are missing required replies");
        videoReportService.sendReminderEmails();
    }
}
