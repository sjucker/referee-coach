package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record UserDTO(@NotNull Long id, @NotNull String name, @NotNull UserRole role) {
}
