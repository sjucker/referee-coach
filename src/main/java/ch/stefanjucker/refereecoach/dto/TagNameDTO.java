package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TagNameDTO(@NotNull @NotBlank @Size(max = 255) String name) {
}
