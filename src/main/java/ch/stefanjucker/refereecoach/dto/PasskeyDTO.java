package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PasskeyDTO(@NotNull Long id, @NotNull String name, @NotNull LocalDateTime createdAt, LocalDateTime lastUsedAt) {
}
