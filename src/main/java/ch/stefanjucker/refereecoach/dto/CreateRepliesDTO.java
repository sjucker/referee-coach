package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateRepliesDTO(@NotNull List<CommentReplyDTO> replies,
                               @NotNull List<VideoCommentDTO> newComments) {
}
