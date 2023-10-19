package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.Fixtures.referee;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_3PO;
import static ch.stefanjucker.refereecoach.service.BasketplanService.Federation.SBL;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.within;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.domain.BasketplanGame;
import ch.stefanjucker.refereecoach.domain.GameDiscussion;
import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionRepository;
import ch.stefanjucker.refereecoach.domain.repository.RefereeRepository;
import ch.stefanjucker.refereecoach.dto.CommentReplyDTO;
import ch.stefanjucker.refereecoach.dto.CreateGameDiscussionCommentDTO;
import ch.stefanjucker.refereecoach.dto.GameDiscussionCommentDTO;
import ch.stefanjucker.refereecoach.util.DateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

class GameDiscussionServiceTest extends AbstractIntegrationTest {

    private static final String GAME_NUMBER = "22-14091";

    @Autowired
    private RefereeRepository refereeRepository;
    @Autowired
    private GameDiscussionRepository gameDiscussionRepository;

    @Autowired
    private GameDiscussionService gameDiscussionService;

    @BeforeEach
    void setUp() {
        refereeRepository.save(referee("Michaelides Markos"));
        refereeRepository.save(referee("Demierre Martin"));
        refereeRepository.save(referee("Balletta Davide"));
    }

    @AfterEach
    void tearDown() {
        gameDiscussionRepository.deleteAll();
        refereeRepository.deleteAll();
    }

    @Test
    void create() {
        // when
        var result = gameDiscussionService.create(SBL, GAME_NUMBER, refereeRepository.findByName("Demierre Martin").orElseThrow());

        // then
        assertThat(result.id()).isNotBlank();
        assertThat(result.basketplanGame().gameNumber()).isEqualTo(GAME_NUMBER);
        assertThat(result.basketplanGame().referee1().name()).isEqualTo("Michaelides Markos");
        assertThat(result.basketplanGame().referee2().name()).isEqualTo("Demierre Martin");
        assertThat(result.basketplanGame().referee3().name()).isEqualTo("Balletta Davide");
        assertThat(result.comments()).isEmpty();

        assertThat(gameDiscussionService.get(result.id())).isPresent();
    }

    @Test
    void create_UnknownGameNumber() {
        var referee = refereeRepository.findByName("Michaelides Markos").orElseThrow();
        assertThatThrownBy(() -> gameDiscussionService.create(SBL, "00-00000", referee)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void create_AlreadyExisting() {
        var referee = refereeRepository.findByName("Michaelides Markos").orElseThrow();
        saveGameDiscussion(GAME_NUMBER);

        assertThatIllegalStateException().isThrownBy(() -> gameDiscussionService.create(SBL, GAME_NUMBER, referee));
    }

    @Test
    void create_RefNotInGame() {
        var referee = refereeRepository.findByName("Michaelides Markos").orElseThrow();
        saveGameDiscussion(GAME_NUMBER);

        assertThatIllegalStateException().isThrownBy(() -> gameDiscussionService.create(SBL, GAME_NUMBER, referee));
    }

    @Test
    void addComments() {
        var referee1 = refereeRepository.findByName("Balletta Davide").orElseThrow();
        var referee2 = refereeRepository.findByName("Michaelides Markos").orElseThrow();

        // when
        var result = gameDiscussionService.create(SBL, GAME_NUMBER, referee1);
        gameDiscussionService.addComments(result.id(), new CreateGameDiscussionCommentDTO(
                List.of(),
                List.of(new GameDiscussionCommentDTO(
                        null,
                        123,
                        "first comment",
                        List.of()
                ))
        ), referee1);
        result = gameDiscussionService.get(result.id()).orElseThrow();
        gameDiscussionService.addComments(result.id(), new CreateGameDiscussionCommentDTO(
                List.of(new CommentReplyDTO(result.comments().get(0).id(), "first reply")),
                List.of()
        ), referee2);

        // then
        assertThat(gameDiscussionService.get(result.id())).hasValueSatisfying(gameDiscussionDTO -> {
            assertThat(gameDiscussionDTO.comments()).hasSize(1)
                                                    .satisfies(comment -> {
                                                        assertThat(comment.timestamp()).isEqualTo(123);
                                                        assertThat(comment.comment()).isEqualTo("Balletta Davide: first comment");
                                                        assertThat(comment.replies()).hasSize(1)
                                                                                     .satisfies(reply -> {
                                                                                         assertThat(reply.repliedBy()).isEqualTo("Michaelides Markos");
                                                                                         assertThat(reply.repliedAt()).isCloseTo(DateUtil.now(), within(1, SECONDS));
                                                                                         assertThat(reply.reply()).isEqualTo("first reply");
                                                                                     }, atIndex(0));
                                                    }, atIndex(0));
        });
        // TODO verify email sent
    }

    private GameDiscussion saveGameDiscussion(String gameNumber) {
        var gameDiscussion = new GameDiscussion();
        gameDiscussion.setId(UUID.randomUUID().toString());
        var basketplanGame = new BasketplanGame();
        basketplanGame.setGameNumber(gameNumber);
        basketplanGame.setCompetition("");
        basketplanGame.setDate(LocalDate.now());
        basketplanGame.setResult("");
        basketplanGame.setTeamA("");
        basketplanGame.setTeamB("");
        basketplanGame.setOfficiatingMode(OFFICIATING_3PO);
        basketplanGame.setYoutubeId("");
        gameDiscussion.setBasketplanGame(basketplanGame);
        return gameDiscussionRepository.save(gameDiscussion);
    }

}
