package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record GameDiscussionCommentReplyDTO(@NotNull Long id,
                                            @NotNull String repliedBy,
                                            @NotNull LocalDateTime repliedAt,
                                            @NotNull String reply) {
}
