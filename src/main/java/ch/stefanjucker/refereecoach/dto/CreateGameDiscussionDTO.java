package ch.stefanjucker.refereecoach.dto;

import ch.stefanjucker.refereecoach.service.BasketplanService.Federation;

import jakarta.validation.constraints.NotNull;

public record CreateGameDiscussionDTO(@NotNull Federation federation,
                                      @NotNull String gameNumber) {
}
