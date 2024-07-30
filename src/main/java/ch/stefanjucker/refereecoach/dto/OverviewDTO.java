package ch.stefanjucker.refereecoach.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

public record OverviewDTO(
        @NotNull String id,
        @NotNull ReportType type,
        @NotNull LocalDate date,
        @NotNull String gameNumber,
        @NotNull String competition,
        @NotNull String teamA,
        @NotNull String teamB,
        UserDTO coach,
        Reportee reportee,
        UserDTO relevantReferee,
        UserDTO referee1,
        UserDTO referee2,
        UserDTO referee3,
        @NotNull Set<Long> relevantRefereeIds,
        boolean finished
) {

    public enum ReportType {
        COACHING,
        GAME_DISCUSSION
    }

    public boolean isVisibleForReferee() {
        return switch (type) {
            case COACHING -> finished;
            case GAME_DISCUSSION -> true;
        };
    }

}
