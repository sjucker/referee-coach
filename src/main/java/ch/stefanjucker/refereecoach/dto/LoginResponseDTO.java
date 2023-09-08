package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record LoginResponseDTO(@NotNull Long id, @NotNull String name, boolean admin, @NotNull UserRole role, @NotNull String jwt) {
}
