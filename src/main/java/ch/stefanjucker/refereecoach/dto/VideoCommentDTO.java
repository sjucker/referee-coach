package ch.stefanjucker.refereecoach.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public record VideoCommentDTO(Long id, // nullable for entries that are not yet persisted
                              @NotNull Integer timestamp,
                              @NotNull @Length(max = 1024) String comment,
                              boolean requiresReply,
                              @NotNull List<VideoCommentReplyDTO> replies,
                              @NotNull Set<TagDTO> tags) {
}
