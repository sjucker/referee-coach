package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;

public record CreateVideoReportDTO(@NotNull String gameNumber,
                                   // potentially manually set by user if not yet available from Basketplan
                                   String youtubeId,
                                   @NotNull Reportee reportee) {
}
