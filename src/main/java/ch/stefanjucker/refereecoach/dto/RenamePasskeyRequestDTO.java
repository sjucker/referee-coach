package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RenamePasskeyRequestDTO(@NotNull @NotBlank String name) {
}
