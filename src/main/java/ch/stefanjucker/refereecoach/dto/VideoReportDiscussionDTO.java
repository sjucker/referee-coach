package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VideoReportDiscussionDTO(@NotNull String videoReportId,
                                       @NotNull BasketplanGameDTO basketplanGame,
                                       @NotNull UserDTO coach,
                                       @NotNull String referee,
                                       @NotNull List<VideoCommentDTO> videoComments) {
}
