package ch.stefanjucker.refereecoach.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "game_discussion_comment")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GameDiscussionComment {

    public static final int COMMENT_MAX_LENGTH = 1024;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer timestamp;

    @Column(nullable = false, length = COMMENT_MAX_LENGTH)
    private String comment;

    @Column(name = "game_discussion_id", nullable = false)
    private String gameDiscussionId;

}
