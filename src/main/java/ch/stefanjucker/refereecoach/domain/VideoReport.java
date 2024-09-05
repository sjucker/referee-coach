package ch.stefanjucker.refereecoach.domain;

import static jakarta.persistence.EnumType.STRING;

import ch.stefanjucker.refereecoach.dto.Reportee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "video_report")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VideoReport {
    // v1: initial report
    // v2: scores/ratings
    // v3: mandatory replies for comments, finishedAt
    public static final int CURRENT_VERSION = 3;

    @Id
    @Column(nullable = false)
    private String id;

    @OneToOne
    @JoinColumn(name = "coach_id")
    private User coach;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Reportee reportee;

    @Embedded
    private BasketplanGame basketplanGame;

    @AttributeOverride(name = "comment", column = @Column(name = "general_comment"))
    @AttributeOverride(name = "score", column = @Column(name = "general_score"))
    @Embedded
    private CriteriaEvaluation general = new CriteriaEvaluation("", 7.0);

    @AttributeOverride(name = "comment", column = @Column(name = "image_comment"))
    @AttributeOverride(name = "score", column = @Column(name = "image_score"))
    @Embedded
    private CriteriaEvaluation image = new CriteriaEvaluation("", 7.0);

    @AttributeOverride(name = "comment", column = @Column(name = "fitness_comment"))
    @AttributeOverride(name = "score", column = @Column(name = "fitness_score"))
    @Embedded
    private CriteriaEvaluation fitness = new CriteriaEvaluation("", 7.0);

    @AttributeOverride(name = "comment", column = @Column(name = "mechanics_comment"))
    @AttributeOverride(name = "score", column = @Column(name = "mechanics_score"))
    @Embedded
    private CriteriaEvaluation mechanics = new CriteriaEvaluation("", 7.0);

    @AttributeOverride(name = "comment", column = @Column(name = "fouls_comment"))
    @AttributeOverride(name = "score", column = @Column(name = "fouls_score"))
    @Embedded
    private CriteriaEvaluation fouls = new CriteriaEvaluation("", 7.0);

    @AttributeOverride(name = "comment", column = @Column(name = "violations_comment"))
    @AttributeOverride(name = "score", column = @Column(name = "violations_score"))
    @Embedded
    private CriteriaEvaluation violations = new CriteriaEvaluation("", 7.0);

    @AttributeOverride(name = "comment", column = @Column(name = "game_management_comment"))
    @AttributeOverride(name = "score", column = @Column(name = "game_management_score"))
    @Embedded
    private CriteriaEvaluation gameManagement = new CriteriaEvaluation("", 7.0);

    @Column(length = 1024, name = "points_to_keep_comment")
    private String pointsToKeepComment;
    @Column(length = 1024, name = "points_to_improve_comment")
    private String pointsToImproveComment;

    @Column(nullable = false)
    private boolean finished;

    // since v3
    private LocalDateTime finishedAt;

    private int version;

    private boolean reminderSent;

    // whether the referee should be promoted
    private boolean promotion;

    public User relevantReferee() {
        return switch (reportee) {
            case FIRST_REFEREE -> basketplanGame.getReferee1();
            case SECOND_REFEREE -> basketplanGame.getReferee2();
            case THIRD_REFEREE -> basketplanGame.getReferee3();
        };
    }

    public Set<Long> relevantRefereeIds() {
        return Set.of(relevantReferee().getId());
    }

}
