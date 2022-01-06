package ch.stefanjucker.refereecoach.dto;

import ch.stefanjucker.refereecoach.service.BasketplanService.Federation;

import javax.validation.constraints.NotNull;

public record CreateVideoReportDTO(@NotNull Federation federation,
                                   @NotNull String gameNumber,
                                   // potentially manually set by user if not yet available from Basketplan
                                   @NotNull String youtubeId,
                                   @NotNull Reportee reportee) {
}
