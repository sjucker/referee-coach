package ch.stefanjucker.refereecoach.domain;

import static jakarta.persistence.CascadeType.MERGE;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "video_report_comment")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VideoComment {

    public static final int COMMENT_MAX_LENGTH = 1024;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer timestamp;

    @Column(nullable = false, length = COMMENT_MAX_LENGTH)
    private String comment;

    @Column(name = "video_report_id", nullable = false)
    private String videoReportId;

    @ManyToMany(cascade = MERGE)
    @JoinTable(
            name = "video_report_comment_tags",
            joinColumns = {@JoinColumn(name = "video_report_comment_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")}
    )
    private Set<Tags> tags = new HashSet<>();

}
