package ch.stefanjucker.refereecoach.domain;

import static jakarta.persistence.EnumType.STRING;

import ch.stefanjucker.refereecoach.dto.OfficiatingMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@ToString(of = "gameNumber")
public class BasketplanGame {

    @Column(nullable = false, name = "game_number")
    private String gameNumber;

    @Column(nullable = false)
    private String competition;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String result;

    @Column(nullable = false, name = "team_a")
    private String teamA;

    @Column(nullable = false, name = "team_b")
    private String teamB;

    @Enumerated(STRING)
    @Column(nullable = false, name = "officiating_mode")
    private OfficiatingMode officiatingMode;

    @OneToOne
    @JoinColumn(name = "referee1_id")
    private User referee1;

    @OneToOne
    @JoinColumn(name = "referee2_id")
    private User referee2;

    @OneToOne
    @JoinColumn(name = "referee3_id")
    private User referee3;

    // can be null if report is done without video comments, text-only
    @Column(name = "youtube_id")
    private String youtubeId;
}
