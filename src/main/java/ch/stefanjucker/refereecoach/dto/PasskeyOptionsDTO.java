package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record PasskeyOptionsDTO(@NotNull String ceremonyId, @NotNull String optionsJson) {
}
