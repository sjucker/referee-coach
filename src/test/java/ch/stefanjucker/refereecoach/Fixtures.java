package ch.stefanjucker.refereecoach;

import static ch.stefanjucker.refereecoach.domain.VideoReport.CURRENT_VERSION;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_2PO;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_3PO;
import static ch.stefanjucker.refereecoach.dto.UserRole.COACH;
import static ch.stefanjucker.refereecoach.dto.UserRole.REFEREE;
import static ch.stefanjucker.refereecoach.dto.UserRole.REFEREE_COACH;
import static ch.stefanjucker.refereecoach.util.DateUtil.now;

import ch.stefanjucker.refereecoach.domain.BasketplanGame;
import ch.stefanjucker.refereecoach.domain.GameDiscussion;
import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.VideoComment;
import ch.stefanjucker.refereecoach.domain.VideoCommentReply;
import ch.stefanjucker.refereecoach.domain.VideoReport;
import ch.stefanjucker.refereecoach.dto.Reportee;

import java.time.LocalDate;

public final class Fixtures {

    private Fixtures() {
    }

    public static User coach(String email) {
        return new User(null, email, email, "", null, null, false, COACH);
    }

    public static User referee(String name) {
        return new User(null, name, name, "", null, null, false, REFEREE);
    }

    public static User refereeCoach(String name) {
        return new User(null, name, name, "", null, null, false, REFEREE_COACH);
    }

    public static VideoReport videoReport(String id, User coach, User referee1, User referee2, User referee3, Reportee reportee) {
        return videoReport(id, "23-00712", LocalDate.now().minusDays(2), coach, referee1, referee2, referee3, reportee);
    }

    public static VideoReport videoReport(String id, String gameNumber, LocalDate gameDate,
                                          User coach, User referee1, User referee2, User referee3,
                                          Reportee reportee) {
        var videoReport = new VideoReport();
        videoReport.setId(id);
        videoReport.setCoach(coach);
        videoReport.setReportee(reportee);
        videoReport.setVersion(CURRENT_VERSION);
        var basketplanGame = basketplanGame(gameNumber, gameDate, referee1, referee2, referee3);
        videoReport.setBasketplanGame(basketplanGame);
        return videoReport;
    }

    public static VideoComment videoComment(String videoReportId, boolean requiresReply) {
        var videoComment = new VideoComment();
        videoComment.setVideoReportId(videoReportId);
        videoComment.setTimestamp(123);
        videoComment.setComment("comment for " + videoReportId);
        videoComment.setRequiresReply(requiresReply);
        return videoComment;
    }

    public static VideoCommentReply videoCommentReply(Long videoCommentId, String repliedBy) {
        var videoCommentReply = new VideoCommentReply();
        videoCommentReply.setRepliedBy(repliedBy);
        videoCommentReply.setRepliedAt(now());
        videoCommentReply.setReply("reply to " + videoCommentId);
        videoCommentReply.setVideoCommentId(videoCommentId);
        return videoCommentReply;
    }

    public static BasketplanGame basketplanGame(String gameNumber, LocalDate date,
                                                User referee1, User referee2, User referee3) {
        var basketplanGame = new BasketplanGame();
        basketplanGame.setGameNumber(gameNumber);
        basketplanGame.setDate(date);
        basketplanGame.setCompetition("");
        basketplanGame.setResult("");
        basketplanGame.setTeamA("");
        basketplanGame.setTeamB("");
        basketplanGame.setOfficiatingMode(referee3 != null ? OFFICIATING_3PO : OFFICIATING_2PO);
        basketplanGame.setYoutubeId("jNQXAC9IVRw");
        basketplanGame.setReferee1(referee1);
        basketplanGame.setReferee2(referee2);
        basketplanGame.setReferee3(referee3);
        return basketplanGame;
    }

    public static GameDiscussion gameDiscussion(String id, String gameNumber, LocalDate gameDate,
                                                User referee1, User referee2, User referee3) {
        var gameDiscussion = new GameDiscussion();
        gameDiscussion.setId(id);
        gameDiscussion.setBasketplanGame(basketplanGame(gameNumber, gameDate, referee1, referee2, referee3));
        return gameDiscussion;
    }

}
