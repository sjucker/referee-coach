package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record ForgotPasswordRequestDTO(@NotNull String email) {
}
