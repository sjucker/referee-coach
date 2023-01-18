package ch.stefanjucker.refereecoach.domain;

import static jakarta.persistence.EnumType.STRING;

import ch.stefanjucker.refereecoach.dto.Reportee;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_report")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class VideoReport {

    public static final int CURRENT_VERSION = 2;

    @Id
    @Column(nullable = false)
    private String id;

    @OneToOne
    @JoinColumn(name = "coach_id")
    private Coach coach;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Reportee reportee;

    @Embedded
    private BasketplanGame basketplanGame;

    @AttributeOverrides({
            @AttributeOverride(name = "comment", column = @Column(name = "general_comment")),
            @AttributeOverride(name = "score", column = @Column(name = "general_score"))
    })
    @Embedded
    private CriteriaEvaluation general = new CriteriaEvaluation("", 7.0);

    @AttributeOverrides({
            @AttributeOverride(name = "comment", column = @Column(name = "image_comment")),
            @AttributeOverride(name = "score", column = @Column(name = "image_score"))
    })
    @Embedded
    private CriteriaEvaluation image = new CriteriaEvaluation("", 7.0);

    @AttributeOverrides({
            @AttributeOverride(name = "comment", column = @Column(name = "fitness_comment")),
            @AttributeOverride(name = "score", column = @Column(name = "fitness_score"))
    })
    @Embedded
    private CriteriaEvaluation fitness = new CriteriaEvaluation("", 7.0);

    @AttributeOverrides({
            @AttributeOverride(name = "comment", column = @Column(name = "mechanics_comment")),
            @AttributeOverride(name = "score", column = @Column(name = "mechanics_score"))
    })
    @Embedded
    private CriteriaEvaluation mechanics = new CriteriaEvaluation("", 7.0);

    @AttributeOverrides({
            @AttributeOverride(name = "comment", column = @Column(name = "fouls_comment")),
            @AttributeOverride(name = "score", column = @Column(name = "fouls_score"))
    })
    @Embedded
    private CriteriaEvaluation fouls = new CriteriaEvaluation("", 7.0);

    @AttributeOverrides({
            @AttributeOverride(name = "comment", column = @Column(name = "violations_comment")),
            @AttributeOverride(name = "score", column = @Column(name = "violations_score"))
    })
    @Embedded
    private CriteriaEvaluation violations = new CriteriaEvaluation("", 7.0);

    @AttributeOverrides({
            @AttributeOverride(name = "comment", column = @Column(name = "game_management_comment")),
            @AttributeOverride(name = "score", column = @Column(name = "game_management_score"))
    })
    @Embedded
    private CriteriaEvaluation gameManagement = new CriteriaEvaluation("", 7.0);

    @Column(length = 1024, name = "points_to_keep_comment")
    private String pointsToKeepComment;
    @Column(length = 1024, name = "points_to_improve_comment")
    private String pointsToImproveComment;

    @Column(nullable = false)
    private boolean finished;

    private int version;

    public Referee relevantReferee() {
        return switch (reportee) {
            case FIRST_REFEREE -> basketplanGame.getReferee1();
            case SECOND_REFEREE -> basketplanGame.getReferee2();
            case THIRD_REFEREE -> basketplanGame.getReferee3();
        };
    }

}
