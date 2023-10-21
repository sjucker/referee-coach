package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.service.BasketplanService.Federation.SBL;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.within;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.Fixtures;
import ch.stefanjucker.refereecoach.dto.CommentReplyDTO;
import ch.stefanjucker.refereecoach.dto.CreateGameDiscussionCommentDTO;
import ch.stefanjucker.refereecoach.dto.GameDiscussionCommentDTO;
import ch.stefanjucker.refereecoach.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

class GameDiscussionServiceTest extends AbstractIntegrationTest {

    private static final String GAME_NUMBER = "22-14091";

    @Autowired
    private GameDiscussionService gameDiscussionService;

    @Test
    void create() {
        // when
        var result = gameDiscussionService.create(SBL, GAME_NUMBER, referee5);

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
        assertThatThrownBy(() -> gameDiscussionService.create(SBL, "00-00000", referee5)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void create_AlreadyExisting() {
        saveGameDiscussion();

        assertThatIllegalStateException().isThrownBy(() -> gameDiscussionService.create(SBL, GAME_NUMBER, referee5));
    }

    @Test
    void create_RefNotInGame() {
        assertThatIllegalStateException().isThrownBy(() -> gameDiscussionService.create(SBL, GAME_NUMBER, referee1));
    }

    @Test
    void addComments() {
        // when
        var result = gameDiscussionService.create(SBL, GAME_NUMBER, referee2);
        gameDiscussionService.addComments(result.id(), new CreateGameDiscussionCommentDTO(
                List.of(),
                List.of(new GameDiscussionCommentDTO(
                        null,
                        123,
                        "first comment",
                        List.of()
                ))
        ), referee2);
        result = gameDiscussionService.get(result.id()).orElseThrow();
        gameDiscussionService.addComments(result.id(), new CreateGameDiscussionCommentDTO(
                List.of(new CommentReplyDTO(result.comments().get(0).id(), "first reply")),
                List.of()
        ), referee4);

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

    private void saveGameDiscussion() {
        gameDiscussionRepository.save(Fixtures.gameDiscussion(UUID.randomUUID().toString(), GAME_NUMBER, LocalDate.now(), referee1, referee2, referee3));
    }

}
