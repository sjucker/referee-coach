package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record PasskeyRegistrationRequestDTO(@NotNull String ceremonyId, @NotNull String credentialJson, String name) {
}
