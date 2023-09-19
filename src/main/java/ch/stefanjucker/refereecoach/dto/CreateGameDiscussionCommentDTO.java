package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateGameDiscussionCommentDTO(@NotNull List<CommentReplyDTO> replies,
                                             @NotNull List<GameDiscussionCommentDTO> newComments) {
}
