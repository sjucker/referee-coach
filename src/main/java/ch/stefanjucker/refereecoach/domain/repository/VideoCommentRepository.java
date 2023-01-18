package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.VideoComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoCommentRepository extends JpaRepository<VideoComment, Long> {

    List<VideoComment> findByVideoReportId(String videoReportId);

    @Query(value = """
            select * from video_report_comment
            where id in (
                select min(vrc.id)
                from video_report r
                         join video_report_comment vrc on r.id = vrc.video_report_id
                where r.game_number = ?1
                  and r.coach_id = ?2
                group by vrc.timestamp
            );
            """,
            nativeQuery = true)
    List<VideoComment> findVideoCommentsByGameNumberAndCoach(String gameNumber, Long coachId);

}
