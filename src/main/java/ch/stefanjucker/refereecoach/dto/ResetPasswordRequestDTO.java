package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record ResetPasswordRequestDTO(@NotNull String email,
                                      @NotNull String token,
                                      @NotNull String newPassword) {
}
