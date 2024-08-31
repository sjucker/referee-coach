package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record CreateGameDiscussionDTO(@NotNull String gameNumber,
                                      @NotNull String youtubeId) {
}
