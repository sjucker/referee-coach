package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.domain.VideoComment.COMMENT_MAX_LENGTH;
import static ch.stefanjucker.refereecoach.domain.VideoReport.CURRENT_VERSION;
import static ch.stefanjucker.refereecoach.util.DateUtil.now;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import ch.stefanjucker.refereecoach.configuration.RefereeCoachProperties;
import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.HasLogin;
import ch.stefanjucker.refereecoach.domain.HasNameEmail;
import ch.stefanjucker.refereecoach.domain.VideoComment;
import ch.stefanjucker.refereecoach.domain.VideoCommentReply;
import ch.stefanjucker.refereecoach.domain.VideoReport;
import ch.stefanjucker.refereecoach.domain.repository.TagsRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoCommentReplyRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoCommentRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoReportRepository;
import ch.stefanjucker.refereecoach.dto.CreateRepliesDTO;
import ch.stefanjucker.refereecoach.dto.Reportee;
import ch.stefanjucker.refereecoach.dto.TagDTO;
import ch.stefanjucker.refereecoach.dto.VideoCommentDTO;
import ch.stefanjucker.refereecoach.dto.VideoReportDTO;
import ch.stefanjucker.refereecoach.dto.VideoReportDiscussionDTO;
import ch.stefanjucker.refereecoach.mapper.DTOMapper;
import ch.stefanjucker.refereecoach.service.BasketplanService.Federation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
public class VideoReportService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private final VideoReportRepository videoReportRepository;
    private final VideoCommentRepository videoCommentRepository;
    private final VideoCommentReplyRepository videoCommentReplyRepository;
    private final TagsRepository tagsRepository;
    private final BasketplanService basketplanService;
    private final JavaMailSender mailSender;
    private final RefereeCoachProperties properties;
    private final Environment environment;

    public VideoReportService(VideoReportRepository videoReportRepository,
                              VideoCommentRepository videoCommentRepository,
                              VideoCommentReplyRepository videoCommentReplyRepository,
                              TagsRepository tagsRepository,
                              BasketplanService basketplanService,
                              JavaMailSender mailSender,
                              RefereeCoachProperties properties,
                              Environment environment) {
        this.videoReportRepository = videoReportRepository;
        this.videoCommentRepository = videoCommentRepository;
        this.videoCommentReplyRepository = videoCommentReplyRepository;
        this.tagsRepository = tagsRepository;
        this.basketplanService = basketplanService;
        this.mailSender = mailSender;
        this.properties = properties;
        this.environment = environment;
    }

    public VideoReportDTO create(Federation federation, String gameNumber, String youtubeId, Reportee reportee, Coach coach) {
        var game = basketplanService.findGameByNumber(federation, gameNumber).orElseThrow();

        var videoReport = new VideoReport();
        videoReport.setId(getUuid());
        var basketplanGame = DTO_MAPPER.fromDTO(game);
        basketplanGame.setYoutubeId(youtubeId); // either coming from Basketplan or manually entered by user
        videoReport.setBasketplanGame(basketplanGame);
        videoReport.setReportee(reportee);
        videoReport.setCoach(coach);
        videoReport.setFinished(false);
        videoReport.setVersion(CURRENT_VERSION);

        return DTO_MAPPER.toDTO(videoReportRepository.save(videoReport), List.of(), getOtherReportees(videoReport));
    }

    private List<Reportee> getOtherReportees(VideoReport videoReport) {
        return videoReportRepository.findByBasketplanGameGameNumberAndCoachId(videoReport.getBasketplanGame().getGameNumber(),
                                                                              videoReport.getCoach().getId())
                                    .stream()
                                    .map(VideoReport::getReportee)
                                    .filter(reportee -> reportee != videoReport.getReportee())
                                    .toList();
    }

    public VideoReportDTO copy(String sourceId, Reportee reportee, Coach coach) {
        var source = videoReportRepository.findById(sourceId).orElseThrow();

        var copy = DTO_MAPPER.copy(source);
        copy.setId(getUuid());
        copy.setCoach(coach);
        copy.setReportee(reportee);
        copy.setFinished(false);
        var newVideoReport = videoReportRepository.save(copy);

        var newComments = videoCommentRepository.saveAll(videoCommentRepository.findByVideoReportId(sourceId).stream()
                                                                               .map(DTO_MAPPER::copy)
                                                                               .peek(commentCopy -> commentCopy.setVideoReportId(newVideoReport.getId()))
                                                                               .toList())
                                                .stream()
                                                .map(DTO_MAPPER::toDTO)
                                                .toList();

        return DTO_MAPPER.toDTO(newVideoReport, newComments, getOtherReportees(newVideoReport));
    }

    public void copyVideoComment(Long sourceId, Reportee reportee, Coach coach) {
        // TODO check that comment does not yet exists in other report
        var source = videoCommentRepository.getReferenceById(sourceId);
        var gameNumber = videoReportRepository.getReferenceById(source.getVideoReportId()).getBasketplanGame().getGameNumber();

        var videoReport = videoReportRepository.findByBasketplanGameGameNumberAndCoachId(gameNumber, coach.getId()).stream()
                                               .filter(s -> s.getReportee() == reportee)
                                               .findFirst()
                                               .orElseThrow();

        var copy = DTO_MAPPER.copy(source);
        copy.setVideoReportId(videoReport.getId());
        videoCommentRepository.save(copy);
    }

    private String getUuid() {
        String uuid;
        do {
            uuid = RandomStringUtils.randomAlphabetic(10);
        } while (videoReportRepository.existsById(uuid));

        return uuid;
    }

    @Transactional
    public VideoReportDTO update(String id, VideoReportDTO dto, Coach coach) {
        var videoReport = videoReportRepository.findById(id).orElseThrow();
        if (!videoReport.getCoach().getEmail().equals(coach.getEmail())) {
            log.error("user {} tried to update video-report {} that does not belong to them", coach, videoReport);
            throw new IllegalStateException("user is not allowed to update this video-report!");
        }

        if (videoReport.isFinished()) {
            log.error("user {} tried to update video-report {} that is already finished", coach, videoReport);
            throw new IllegalStateException("user is not allowed to update already finished video-report!");
        }

        DTO_MAPPER.update(dto, videoReport);
        videoReport = videoReportRepository.save(videoReport);
        Set<Long> existingVideoCommentIds = videoCommentRepository.findByVideoReportId(id).stream()
                                                                  .map(VideoComment::getId)
                                                                  .collect(toCollection(HashSet::new));

        List<VideoCommentDTO> comments = new ArrayList<>();
        for (var videoCommentDTO : dto.videoComments()) {
            var videoComment = videoCommentRepository.save(DTO_MAPPER.fromDTO(videoCommentDTO, dto.id()));
            comments.add(DTO_MAPPER.toDTO(videoComment));
            existingVideoCommentIds.remove(videoComment.getId());
        }
        videoCommentRepository.deleteAllById(existingVideoCommentIds);

        if (videoReport.isFinished()) {
            var simpleMessage = new SimpleMailMessage();
            try {
                var referee = videoReport.relevantReferee();
                log.info("finishing {}, send email to {}", videoReport, referee.getEmail());

                simpleMessage.setSubject(dto.isTextOnly() ? "[Referee Coach] New Report" : "[Referee Coach] New Video Report");
                simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
                simpleMessage.setReplyTo(videoReport.getCoach().getEmail());
                simpleMessage.setBcc(properties.getBccMail());

                if (properties.isOverrideRecipient()) {
                    simpleMessage.setTo(videoReport.getCoach().getEmail());
                    simpleMessage.setSubject(simpleMessage.getSubject() + " (%s)".formatted(referee.getEmail()));
                } else {
                    simpleMessage.setTo(referee.getEmail());
                    // make sure that copy-receiver does not receive mail twice when he is the coach
                    simpleMessage.setCc(Stream.of(videoReport.getCoach().getEmail(), properties.getCcMail())
                                              .distinct()
                                              .toArray(String[]::new));
                }

                if (dto.isTextOnly()) {
                    // text-only report does not video comments discussion
                    simpleMessage.setText(("Hi %s%n%nYour report is ready.%nPlease visit: %s/#/view/%s")
                                                  .formatted(referee.getName(),
                                                             properties.getBaseUrl(),
                                                             dto.id()));

                } else {
                    simpleMessage.setText(("Hi %s%n%nYour video report is ready.%nPlease visit: %s/#/view/%s" +
                            "%nFor discussion of the comments, use the following: %s/#/discuss/%s")
                                                  .formatted(referee.getName(),
                                                             properties.getBaseUrl(),
                                                             dto.id(),
                                                             properties.getBaseUrl(),
                                                             dto.id()));
                }

                mailSender.send(simpleMessage);
            } catch (MailException e) {
                log.error("could not send email to: " + Arrays.toString(simpleMessage.getTo()), e);
            }
        }
        return DTO_MAPPER.toDTO(videoReport, comments, getOtherReportees(videoReport));
    }

    public Optional<VideoReportDTO> find(String id) {

        List<VideoCommentDTO> videoCommentDTOs = new ArrayList<>();
        for (var videoComment : videoCommentRepository.findByVideoReportId(id)) {
            var replies = videoCommentReplyRepository.findByVideoCommentIdOrderByRepliedAt(videoComment.getId());
            videoCommentDTOs.add(DTO_MAPPER.toDTO(videoComment, DTO_MAPPER.toDTO(replies)));
        }

        return videoReportRepository.findById(id)
                                    .map(videoReport -> DTO_MAPPER.toDTO(videoReport, videoCommentDTOs, getOtherReportees(videoReport)));
    }

    public void delete(String id, Coach coach) {
        // verify, that us is allowed to delete this video report
        var videoReport = videoReportRepository.findById(id).orElseThrow();
        if (coach.isAdmin() || isUnfinishedReportOwnedByUser(videoReport, coach)) {
            videoReportRepository.delete(videoReport);
        } else {
            log.error("user ({}) tried to delete video report ({}), but is not authorized to do so", coach, videoReport);
            throw new IllegalStateException("user is not allowed to delete this video-report!");
        }
    }

    public VideoReportDiscussionDTO getVideoReportDiscussion(String videoReportId) {
        var videoReport = videoReportRepository.findById(videoReportId).orElseThrow();

        List<VideoCommentDTO> videoCommentDTOs = new ArrayList<>();
        Set<Integer> timestamps = new HashSet<>();
        for (var videoComment : videoCommentRepository.findByVideoReportId(videoReportId).stream()
                                                      .filter(VideoComment::isRequiresReply)
                                                      .toList()) {
            var replies = videoCommentReplyRepository.findByVideoCommentIdOrderByRepliedAt(videoComment.getId());
            videoCommentDTOs.add(DTO_MAPPER.toDTO(videoComment, DTO_MAPPER.toDTO(replies)));
            timestamps.add(videoComment.getTimestamp());
        }

        for (var videoComment : videoCommentRepository.findVideoCommentsByGameNumberAndCoach(videoReport.getBasketplanGame().getGameNumber(),
                                                                                             videoReport.getCoach().getId()).stream()
                                                      .filter(videoComment -> !timestamps.contains(videoComment.getTimestamp()))
                                                      .toList()) {
            var replies = videoCommentReplyRepository.findByVideoCommentIdOrderByRepliedAt(videoComment.getId());
            videoCommentDTOs.add(DTO_MAPPER.toDTO(videoComment, DTO_MAPPER.toDTO(replies)));
        }

        return new VideoReportDiscussionDTO(videoReport.getId(),
                                            DTO_MAPPER.toDTO(videoReport.getBasketplanGame()),
                                            DTO_MAPPER.toDTO(videoReport.getCoach()),
                                            videoReport.relevantReferee().getName(),
                                            videoCommentDTOs.stream()
                                                            .sorted(comparing(VideoCommentDTO::timestamp))
                                                            .toList());
    }

    private boolean isUnfinishedReportOwnedByUser(VideoReport videoReport, Coach coach) {
        return !videoReport.isFinished() && videoReport.getCoach().getEmail().equals(coach.getEmail());
    }

    public void addReplies(HasLogin user, String id, CreateRepliesDTO dto) {
        var videoReport = videoReportRepository.findById(id).orElseThrow();

        String repliedBy;
        if (user != null) {
            repliedBy = user.getName();
        } else {
            repliedBy = videoReport.relevantReferee().getName();
        }

        for (var reply : dto.replies()) {
            videoCommentReplyRepository.save(new VideoCommentReply(null, repliedBy, now(), reply.comment(), reply.commentId()));
        }

        boolean newCommentsMade = false;
        for (var newComment : dto.newComments()) {
            if (StringUtils.isNotEmpty(newComment.comment())) {
                videoCommentRepository.save(new VideoComment(null, newComment.timestamp(),
                                                             abbreviate("%s: %s".formatted(repliedBy, newComment.comment()), COMMENT_MAX_LENGTH),
                                                             id, false, new HashSet<>()));
                newCommentsMade = true;
            }
        }

        for (var report : videoReportRepository.findByBasketplanGameGameNumberAndCoachId(videoReport.getBasketplanGame().getGameNumber(),
                                                                                         videoReport.getCoach().getId())) {

            if ((user != null && user.isCoach()) || !report.getId().equals(id)) {
                sendDiscussionEmail(repliedBy, report.relevantReferee(), report.getId(), newCommentsMade);
            }
        }

        // user == null => one of the referees replied
        // user != coach => another coach replied
        // in both cases send to the coach
        if (user == null || user.isReferee() || (user.isCoach() && !user.getId().equals(videoReport.getCoach().getId()))) {
            sendDiscussionEmail(repliedBy, videoReport.getCoach(), videoReport.getId(), newCommentsMade);
        }
    }

    private void sendDiscussionEmail(String repliedBy, HasNameEmail recipient, String reportId, boolean newCommentsMade) {
        var simpleMessage = new SimpleMailMessage();
        try {
            simpleMessage.setSubject("[Referee Coach] New Video Report Discussion");
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setBcc(properties.getBccMail());

            if (properties.isOverrideRecipient()) {
                simpleMessage.setTo(properties.getOverrideRecipientMail());
                simpleMessage.setSubject(simpleMessage.getSubject() + " (%s)".formatted(recipient.getEmail()));
            } else {
                simpleMessage.setTo(recipient.getEmail());
            }
            if (newCommentsMade) {
                simpleMessage.setText("Hi %s%n%n%s added new replies to a video report.%nAlso, there are comments for new scenes.%nPlease visit: %s/#/discuss/%s".formatted(
                        recipient.getName(), repliedBy, properties.getBaseUrl(), reportId));
            } else {
                simpleMessage.setText("Hi %s%n%n%s added new replies to a video report.%nPlease visit: %s/#/discuss/%s".formatted(
                        recipient.getName(), repliedBy, properties.getBaseUrl(), reportId));
            }

            log.info("sending email to {}, text: {}", recipient.getEmail(), simpleMessage.getText());

            mailSender.send(simpleMessage);
        } catch (MailException e) {
            log.error("could not send email to: " + Arrays.toString(simpleMessage.getTo()), e);
        }
    }

    public List<TagDTO> getAllAvailableTags() {
        return tagsRepository.findAll().stream()
                             .map(DTO_MAPPER::toDTO)
                             .toList();

    }

    public void sendReminderEmails() {
        // referee has 48 hours to reply to required comments
        for (var videoReportId : videoReportRepository.findReportIdsWithMissingReplies(now().minusDays(2))) {
            var videoReport = videoReportRepository.findById(videoReportId).orElseThrow();
            sendReminderEmail(videoReport.relevantReferee(), videoReportId);
            videoReport.setReminderSent(true);
            videoReportRepository.save(videoReport);
        }
    }

    private void sendReminderEmail(HasNameEmail recipient, String reportId) {
        var simpleMessage = new SimpleMailMessage();
        try {
            simpleMessage.setSubject("[Referee Coach] Reminder Required Replies");
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setBcc(properties.getBccMail());

            if (properties.isOverrideRecipient()) {
                simpleMessage.setTo(properties.getOverrideRecipientMail());
                simpleMessage.setSubject(simpleMessage.getSubject() + " (%s)".formatted(recipient.getEmail()));
            } else {
                simpleMessage.setTo(recipient.getEmail());
                simpleMessage.setCc(properties.getCcMail());
            }

            simpleMessage.setText("Hi %s%n%nThis is a reminder that you have not yet replied to all important comments.%nPlease visit: %s/#/discuss/%s".formatted(
                    recipient.getName(), properties.getBaseUrl(), reportId));

            log.info("sending reminder email to {}, text: {}", recipient.getEmail(), simpleMessage.getText());

            mailSender.send(simpleMessage);
        } catch (MailException e) {
            log.error("could not send reminder email to: " + Arrays.toString(simpleMessage.getTo()), e);
        }
    }

    public void updateMissingScores() {
        for (var videoReport : videoReportRepository.findReportsWithMissingResult().stream()
                                                    // do not make too many calls to Basketplan at once
                                                    .limit(50)
                                                    .toList()) {
            basketplanService.findGameByNumber(videoReport.getBasketplanGame().getGameNumber()).ifPresent(dto -> {
                log.info("update result of video report {} from {} to {}", videoReport.getId(), videoReport.getBasketplanGame().getResult(), dto.result());
                videoReport.getBasketplanGame().setResult(dto.result());
                videoReportRepository.save(videoReport);
            });
        }
    }
}
