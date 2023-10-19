package ch.stefanjucker.refereecoach;

import static ch.stefanjucker.refereecoach.domain.Referee.RefereeLevel.GROUP_1;

import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.Referee;

public final class Fixtures {

    public static final String COACH_EMAIL = "coach@referee-coach.ch";

    private Fixtures() {
    }

    public static Coach coach() {
        return coach(COACH_EMAIL);
    }

    public static Coach coach(String email) {
        return new Coach(null, email, "Coach", "", false, null);
    }

    public static Referee referee(String name) {
        return new Referee(null, name, name, GROUP_1, "", null);
    }

}
