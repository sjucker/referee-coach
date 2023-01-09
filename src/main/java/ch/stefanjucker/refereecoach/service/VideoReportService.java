package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.domain.VideoReport.CURRENT_VERSION;
import static java.util.stream.Collectors.toCollection;

import ch.stefanjucker.refereecoach.configuration.RefereeCoachProperties;
import ch.stefanjucker.refereecoach.domain.HasNameEmail;
import ch.stefanjucker.refereecoach.domain.User;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private static final String STEFAN_JUCKER_EMAIL = "reports@referee-coach.ch";
    private static final String FABRIZIO_PIZIO_EMAIL = "fabrizio.pizio@swissbasketball.ch";

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

    public VideoReportDTO create(Federation federation, String gameNumber, String youtubeId, Reportee reportee, User user) {
        var game = basketplanService.findGameByNumber(federation, gameNumber).orElseThrow();

        var videoReport = new VideoReport();
        videoReport.setId(getUuid());
        var basketplanGame = DTO_MAPPER.fromDTO(game);
        basketplanGame.setYoutubeId(youtubeId); // either coming from Basketplan or manually entered by user
        videoReport.setBasketplanGame(basketplanGame);
        videoReport.setReportee(reportee);
        videoReport.setReporter(user);
        videoReport.setFinished(false);
        videoReport.setVersion(CURRENT_VERSION);

        return DTO_MAPPER.toDTO(videoReportRepository.save(videoReport), List.of(), getOtherReportees(videoReport));
    }

    private List<Reportee> getOtherReportees(VideoReport videoReport) {
        return videoReportRepository.findByBasketplanGameGameNumberAndReporterId(videoReport.getBasketplanGame().getGameNumber(),
                                                                                 videoReport.getReporter().getId())
                                    .stream()
                                    .map(VideoReport::getReportee)
                                    .filter(reportee -> reportee != videoReport.getReportee())
                                    .toList();
    }

    public VideoReportDTO copy(String sourceId, Reportee reportee, User user) {
        var source = videoReportRepository.findById(sourceId).orElseThrow();

        var copy = DTO_MAPPER.copy(source);
        copy.setId(getUuid());
        copy.setReporter(user);
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

    public void copyVideoComment(Long sourceId, Reportee reportee, User user) {
        // TODO check that comment does not yet exists in other report
        var source = videoCommentRepository.getReferenceById(sourceId);
        var gameNumber = videoReportRepository.getReferenceById(source.getVideoReportId()).getBasketplanGame().getGameNumber();

        var videoReport = videoReportRepository.findByBasketplanGameGameNumberAndReporterId(gameNumber, user.getId()).stream()
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
    public VideoReportDTO update(String id, VideoReportDTO dto, User user) {
        var videoReport = videoReportRepository.findById(id).orElseThrow();
        if (!videoReport.getReporter().getEmail().equals(user.getEmail())) {
            log.error("user {} tried to update video-report {} that does not belong to them", user, videoReport);
            throw new IllegalStateException("user is not allowed to update this video-report!");
        }

        if (videoReport.isFinished()) {
            log.error("user {} tried to update video-report {} that is already finished", user, videoReport);
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
            SimpleMailMessage simpleMessage = new SimpleMailMessage();
            try {
                var referee = videoReport.relevantReferee();
                log.info("finishing {}, send email to {}", videoReport, referee.getEmail());

                simpleMessage.setSubject(dto.isTextOnly() ? "[Referee Coach] New Report" : "[Referee Coach] New Video Report");
                simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
                simpleMessage.setReplyTo(videoReport.getReporter().getEmail());
                simpleMessage.setBcc(STEFAN_JUCKER_EMAIL);

                if (properties.isOverrideRecipient()) {
                    simpleMessage.setTo(videoReport.getReporter().getEmail());
                    simpleMessage.setSubject(simpleMessage.getSubject() + " (%s)".formatted(referee.getEmail()));
                } else {
                    simpleMessage.setTo(referee.getEmail());
                    // make sure Fabrizio does not receive mail twice when he is the reporter
                    simpleMessage.setCc(Stream.of(videoReport.getReporter().getEmail(), FABRIZIO_PIZIO_EMAIL)
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

    public void delete(String id, User user) {
        // verify, that us is allowed to delete this video report
        var videoReport = videoReportRepository.findById(id).orElseThrow();
        if (user.isAdmin() || isUnfinishedReportOwnedByUser(videoReport, user)) {
            videoReportRepository.delete(videoReport);
        } else {
            log.error("user ({}) tried to delete video report ({}), but is not authorized to do so", user, videoReport);
            throw new IllegalStateException("user is not allowed to delete this video-report!");
        }
    }

    public VideoReportDiscussionDTO getVideoReportDiscussion(String videoReportId) {
        var videoReport = videoReportRepository.findById(videoReportId).orElseThrow();

        List<VideoCommentDTO> videoCommentDTOs = new ArrayList<>();
        for (var videoComment : videoCommentRepository.findVideoCommentsByGameNumberAndReporter(videoReport.getBasketplanGame()
                                                                                                           .getGameNumber(), videoReport.getReporter()
                                                                                                                                        .getId())) {
            var replies = videoCommentReplyRepository.findByVideoCommentIdOrderByRepliedAt(videoComment.getId());
            videoCommentDTOs.add(DTO_MAPPER.toDTO(videoComment, DTO_MAPPER.toDTO(replies)));
        }

        return new VideoReportDiscussionDTO(videoReport.getId(),
                                            DTO_MAPPER.toDTO(videoReport.getBasketplanGame()),
                                            DTO_MAPPER.toDTO(videoReport.getReporter()),
                                            videoReport.relevantReferee().getName(),
                                            videoCommentDTOs);
    }

    private boolean isUnfinishedReportOwnedByUser(VideoReport videoReport, User user) {
        return !videoReport.isFinished() && videoReport.getReporter().getEmail().equals(user.getEmail());
    }

    public List<VideoReportDTO> findAll(LocalDate from, LocalDate to) {
        return videoReportRepository.findAll(from, to)
                                    .stream()
                                    .map(DTO_MAPPER::toDTO) // no need to fill the comments as well, not needed in report overview
                                    .toList();
    }

    public void addReplies(User user, String id, CreateRepliesDTO dto) {
        var videoReport = videoReportRepository.findById(id).orElseThrow();

        String repliedBy;
        if (user != null) {
            repliedBy = user.getName();
        } else {
            repliedBy = videoReport.relevantReferee().getName();
        }

        for (var reply : dto.replies()) {
            videoCommentReplyRepository.save(new VideoCommentReply(null, repliedBy, LocalDateTime.now(), reply.comment(), reply.commentId()));
        }

        boolean newCommentsMade = false;
        for (var newComment : dto.newComments()) {
            if (StringUtils.isNotEmpty(newComment.comment())) {
                videoCommentRepository.save(new VideoComment(null, newComment.timestamp(), "%s: %s".formatted(repliedBy, newComment.comment()), id, new HashSet<>()));
                newCommentsMade = true;
            }
        }

        for (var report : videoReportRepository.findByBasketplanGameGameNumberAndReporterId(videoReport.getBasketplanGame().getGameNumber(),
                                                                                            videoReport.getReporter().getId())) {

            if (user != null || !report.getId().equals(id)) {
                sendDiscussionEmail(repliedBy, report.relevantReferee(), report.getId(), newCommentsMade);
            }
        }

        // user == null => one of the referees replied
        // user != reporter => another coach replied
        // in both cases send to the reporter
        if (user == null || !user.getId().equals(videoReport.getReporter().getId())) {
            sendDiscussionEmail(repliedBy, videoReport.getReporter(), videoReport.getId(), newCommentsMade);
        }
    }

    private void sendDiscussionEmail(String repliedBy, HasNameEmail recipient, String reportId, boolean newCommentsMade) {
        SimpleMailMessage simpleMessage = new SimpleMailMessage();
        try {
            simpleMessage.setSubject("[Referee Coach] New Video Report Discussion");
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setBcc(STEFAN_JUCKER_EMAIL);

            if (properties.isOverrideRecipient()) {
                simpleMessage.setTo(STEFAN_JUCKER_EMAIL);
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
}
