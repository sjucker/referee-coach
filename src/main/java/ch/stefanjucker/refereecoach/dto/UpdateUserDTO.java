package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserDTO(@NotNull String name,
                            @NotNull String email,
                            @NotNull UserRole role,
                            @NotNull boolean admin) {
}
