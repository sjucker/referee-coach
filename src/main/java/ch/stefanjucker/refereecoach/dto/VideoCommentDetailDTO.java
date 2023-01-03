package ch.stefanjucker.refereecoach.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record VideoCommentDetailDTO(@NotNull String gameNumber,
                                    @NotNull String competition,
                                    @NotNull LocalDate date,
                                    @NotNull Integer timestamp,
                                    @NotNull @Length(max = 1024) String comment,
                                    @NotNull String youtubeId,
                                    @NotNull String tags) {
}
