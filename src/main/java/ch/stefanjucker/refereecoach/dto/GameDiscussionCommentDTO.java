package ch.stefanjucker.refereecoach.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GameDiscussionCommentDTO(Long id, // nullable for entries that are not yet persisted
                                       @NotNull Integer timestamp,
                                       @Column(nullable = false, length = 1024)
                                       @NotNull String comment,
                                       @NotNull List<GameDiscussionCommentReplyDTO> replies) {
}
