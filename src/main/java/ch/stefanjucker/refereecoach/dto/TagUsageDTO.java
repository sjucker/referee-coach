package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record TagUsageDTO(@NotNull Long id,
                          @NotNull String name,
                          long usageCount) {
}
