package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.domain.GameDiscussionComment.COMMENT_MAX_LENGTH;
import static ch.stefanjucker.refereecoach.util.DateUtil.now;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import ch.stefanjucker.refereecoach.configuration.RefereeCoachProperties;
import ch.stefanjucker.refereecoach.domain.GameDiscussion;
import ch.stefanjucker.refereecoach.domain.GameDiscussionComment;
import ch.stefanjucker.refereecoach.domain.GameDiscussionCommentReply;
import ch.stefanjucker.refereecoach.domain.HasLogin;
import ch.stefanjucker.refereecoach.domain.HasNameEmail;
import ch.stefanjucker.refereecoach.domain.Referee;
import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionCommentReplyRepository;
import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionCommentRepository;
import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionRepository;
import ch.stefanjucker.refereecoach.dto.CreateGameDiscussionCommentDTO;
import ch.stefanjucker.refereecoach.dto.GameDiscussionCommentDTO;
import ch.stefanjucker.refereecoach.dto.GameDiscussionCommentReplyDTO;
import ch.stefanjucker.refereecoach.dto.GameDiscussionDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GameDiscussionService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private final GameDiscussionRepository gameDiscussionRepository;
    private final GameDiscussionCommentRepository gameDiscussionCommentRepository;
    private final GameDiscussionCommentReplyRepository gameDiscussionCommentReplyRepository;
    private final BasketplanService basketplanService;
    private final JavaMailSender mailSender;
    private final RefereeCoachProperties properties;
    private final Environment environment;

    public GameDiscussionService(GameDiscussionRepository gameDiscussionRepository,
                                 GameDiscussionCommentRepository gameDiscussionCommentRepository,
                                 GameDiscussionCommentReplyRepository gameDiscussionCommentReplyRepository,
                                 BasketplanService basketplanService,
                                 JavaMailSender mailSender,
                                 RefereeCoachProperties properties,
                                 Environment environment) {
        this.gameDiscussionRepository = gameDiscussionRepository;
        this.gameDiscussionCommentRepository = gameDiscussionCommentRepository;
        this.gameDiscussionCommentReplyRepository = gameDiscussionCommentReplyRepository;

        this.basketplanService = basketplanService;
        this.mailSender = mailSender;
        this.properties = properties;
        this.environment = environment;
    }

    public GameDiscussionDTO create(Federation federation, String gameNumber, String youtubeId, HasLogin referee) {
        if (gameDiscussionRepository.findByBasketplanGameGameNumber(gameNumber).isPresent()) {
            throw new IllegalArgumentException("there exists already a game discussion for game number: %s".formatted(gameNumber));
        }

        if (StringUtils.isBlank(youtubeId)) {
            throw new IllegalStateException("invalid youtubeId given: %s".formatted(youtubeId));
        }

        var game = basketplanService.findGameByNumber(federation, gameNumber).orElseThrow();
        if (game.referee1() != null && game.referee1().id().equals(referee.getId()) ||
                game.referee2() != null && game.referee2().id().equals(referee.getId()) ||
                game.referee3() != null && game.referee3().id().equals(referee.getId())) {

            var gameDiscussion = new GameDiscussion();
            gameDiscussion.setId(getUuid());
            var basketplanGame = DTO_MAPPER.fromDTO(game);
            basketplanGame.setYoutubeId(youtubeId);
            gameDiscussion.setBasketplanGame(basketplanGame);
            gameDiscussion = gameDiscussionRepository.save(gameDiscussion);

            return new GameDiscussionDTO(gameDiscussion.getId(),
                                         DTO_MAPPER.toDTO(gameDiscussion.getBasketplanGame()),
                                         List.of());
        } else {
            throw new IllegalStateException("referee %s tried to create a game discussion to which they do not belong to".formatted(referee));
        }
    }

    private String getUuid() {
        String uuid;
        do {
            uuid = RandomStringUtils.randomAlphabetic(10);
        } while (gameDiscussionRepository.existsById(uuid));

        return uuid;
    }

    public Optional<GameDiscussionDTO> get(String id) {
        return gameDiscussionRepository.findById(id)
                                       .map(gameDiscussion -> new GameDiscussionDTO(
                                               gameDiscussion.getId(),
                                               DTO_MAPPER.toDTO(gameDiscussion.getBasketplanGame()),
                                               gameDiscussionCommentRepository.findByGameDiscussionId(gameDiscussion.getId())
                                                                              .stream()
                                                                              .map(comment -> new GameDiscussionCommentDTO(
                                                                                      comment.getId(),
                                                                                      comment.getTimestamp(),
                                                                                      comment.getComment(),
                                                                                      gameDiscussionCommentReplyRepository.findByGameDiscussionCommentIdOrderByRepliedAt(comment.getId())
                                                                                                                          .stream()
                                                                                                                          .map(reply -> new GameDiscussionCommentReplyDTO(
                                                                                                                                  reply.getId(),
                                                                                                                                  reply.getRepliedBy(),
                                                                                                                                  reply.getRepliedAt(),
                                                                                                                                  reply.getReply()
                                                                                                                          ))
                                                                                                                          .toList()
                                                                              ))
                                                                              .toList()
                                       ));
    }

    public void addComments(String id, CreateGameDiscussionCommentDTO dto, HasLogin user) {
        var gameDiscussion = gameDiscussionRepository.findById(id).orElseThrow();
        var repliedBy = user.getName();

        for (var reply : dto.replies()) {
            gameDiscussionCommentReplyRepository.save(new GameDiscussionCommentReply(null, repliedBy, now(), reply.comment(), reply.commentId()));
        }

        for (var newComment : dto.newComments()) {
            if (StringUtils.isNotEmpty(newComment.comment())) {
                gameDiscussionCommentRepository.save(new GameDiscussionComment(null,
                                                                               newComment.timestamp(),
                                                                               abbreviate("%s: %s".formatted(repliedBy, newComment.comment()), COMMENT_MAX_LENGTH),
                                                                               id));
            }
        }

        var game = gameDiscussion.getBasketplanGame();
        sendEmailIfNeeded(game.getReferee1(), user, repliedBy, id);
        sendEmailIfNeeded(game.getReferee2(), user, repliedBy, id);
        sendEmailIfNeeded(game.getReferee3(), user, repliedBy, id);
        sendCopy(repliedBy, id);
    }

    private void sendEmailIfNeeded(Referee otherReferee, HasLogin commenter, String repliedBy, String id) {
        if (otherReferee != null && (commenter.isCoach() || !otherReferee.getId().equals(commenter.getId()))) {
            sendEmail(repliedBy, otherReferee, id);
        }
    }

    private void sendEmail(String repliedBy, HasNameEmail recipient, String gameDiscussionId) {
        var simpleMessage = new SimpleMailMessage();
        try {
            simpleMessage.setSubject("[Referee Coach] New Game Discussion Comments");
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setBcc(properties.getBccMail());

            if (properties.isOverrideRecipient()) {
                simpleMessage.setTo(properties.getOverrideRecipientMail());
                simpleMessage.setSubject(simpleMessage.getSubject() + " (%s)".formatted(recipient.getEmail()));
            } else {
                simpleMessage.setTo(recipient.getEmail());
            }
            simpleMessage.setText("Hi %s%n%n%s added new comments to a game discussion.%nPlease visit: %s/#/game-discussion/%s".formatted(
                    recipient.getName(), repliedBy, properties.getBaseUrl(), gameDiscussionId));

            log.info("sending email to {}, text: {}", recipient.getEmail(), simpleMessage.getText());

            mailSender.send(simpleMessage);
        } catch (MailException e) {
            log.error("could not send email to: " + Arrays.toString(simpleMessage.getTo()), e);
        }
    }

    private void sendCopy(String repliedBy, String gameDiscussionId) {
        var simpleMessage = new SimpleMailMessage();
        try {
            simpleMessage.setSubject("[Referee Coach] New Game Discussion Comments");
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setTo(properties.getCcMail());

            simpleMessage.setText("%s added new comments to a game discussion.%nPlease visit: %s/#/game-discussion/%s".formatted(
                    repliedBy, properties.getBaseUrl(), gameDiscussionId));

            log.info("sending email to {}, text: {}", properties.getCcMail(), simpleMessage.getText());

            mailSender.send(simpleMessage);
        } catch (MailException e) {
            log.error("could not send email to: " + Arrays.toString(simpleMessage.getTo()), e);
        }
    }
}
