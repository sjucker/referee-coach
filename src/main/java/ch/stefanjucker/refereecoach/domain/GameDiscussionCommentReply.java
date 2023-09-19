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
import java.time.LocalDateTime;

@Entity
@Table(name = "game_discussion_comment_reply")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GameDiscussionCommentReply {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String repliedBy;

    @Column(nullable = false)
    private LocalDateTime repliedAt;

    @Column(nullable = false, length = 1024)
    private String reply;

    @Column(name = "game_discussion_comment_id", nullable = false)
    private Long gameDiscussionCommentId;
}
