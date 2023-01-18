package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record CoachDTO(@NotNull Long id, @NotNull String name) {
}
