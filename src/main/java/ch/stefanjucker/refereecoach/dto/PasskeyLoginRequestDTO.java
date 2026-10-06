package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record PasskeyLoginRequestDTO(@NotNull String ceremonyId, @NotNull String credentialJson) {
}
