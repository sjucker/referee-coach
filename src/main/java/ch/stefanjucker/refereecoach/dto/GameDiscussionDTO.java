package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GameDiscussionDTO(@NotNull String id,
                                @NotNull BasketplanGameDTO basketplanGame,
                                @NotNull List<GameDiscussionCommentDTO> comments) {
}
