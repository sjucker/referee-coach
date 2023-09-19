package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BasketplanGameDTO(@NotNull String gameNumber,
                                @NotNull String competition,
                                @NotNull LocalDate date,
                                @NotNull String result,
                                @NotNull String teamA,
                                @NotNull String teamB,
                                @NotNull OfficiatingMode officiatingMode,
                                // all referees are nullable if not found in database
                                RefereeDTO referee1,
                                RefereeDTO referee2,
                                RefereeDTO referee3,
                                String youtubeId) {

    public boolean containsReferee(Long refereeId) {
        return (referee1 != null && referee1.id().equals(refereeId)) ||
                (referee2 != null && referee2.id().equals(refereeId)) ||
                (referee3 != null && referee3.id().equals(refereeId));
    }
}
