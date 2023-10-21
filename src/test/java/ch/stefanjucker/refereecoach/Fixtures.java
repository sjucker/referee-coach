package ch.stefanjucker.refereecoach;

import static ch.stefanjucker.refereecoach.domain.Referee.RefereeLevel.GROUP_1;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_2PO;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_3PO;

import ch.stefanjucker.refereecoach.domain.BasketplanGame;
import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.GameDiscussion;
import ch.stefanjucker.refereecoach.domain.Referee;
import ch.stefanjucker.refereecoach.domain.VideoReport;
import ch.stefanjucker.refereecoach.dto.Reportee;

import java.time.LocalDate;

public final class Fixtures {

    private Fixtures() {
    }

    public static Coach coach(String email) {
        return new Coach(null, email, email, "", false, null);
    }

    public static Referee referee(String name) {
        return new Referee(null, name, name, GROUP_1, "", null);
    }

    public static VideoReport videoReport(String id, String gameNumber, LocalDate gameDate,
                                          Coach coach, Referee referee1, Referee referee2, Referee referee3,
                                          Reportee reportee) {
        var videoReport = new VideoReport();
        videoReport.setId(id);
        videoReport.setCoach(coach);
        videoReport.setReportee(reportee);
        var basketplanGame = basketplanGame(gameNumber, gameDate, referee1, referee2, referee3);
        videoReport.setBasketplanGame(basketplanGame);
        return videoReport;
    }

    public static BasketplanGame basketplanGame(String gameNumber, LocalDate date,
                                                Referee referee1, Referee referee2, Referee referee3) {
        var basketplanGame = new BasketplanGame();
        basketplanGame.setGameNumber(gameNumber);
        basketplanGame.setDate(date);
        basketplanGame.setCompetition("");
        basketplanGame.setResult("");
        basketplanGame.setTeamA("");
        basketplanGame.setTeamB("");
        basketplanGame.setOfficiatingMode(referee3 != null ? OFFICIATING_3PO : OFFICIATING_2PO);
        basketplanGame.setYoutubeId("");
        basketplanGame.setReferee1(referee1);
        basketplanGame.setReferee2(referee2);
        basketplanGame.setReferee3(referee3);
        return basketplanGame;
    }

    public static GameDiscussion gameDiscussion(String id, String gameNumber, LocalDate gameDate,
                                                Referee referee1, Referee referee2, Referee referee3) {
        var gameDiscussion = new GameDiscussion();
        gameDiscussion.setId(id);
        gameDiscussion.setBasketplanGame(basketplanGame(gameNumber, gameDate, referee1, referee2, referee3));
        return gameDiscussion;
    }

}
