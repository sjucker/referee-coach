package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record CreateUserDTO(@NotNull String name,
                            @NotNull String email,
                            @NotNull UserRole role,
                            @NotNull boolean admin,
                            @NotNull String password) {
}
