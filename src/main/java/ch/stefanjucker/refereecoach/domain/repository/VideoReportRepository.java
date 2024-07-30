package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.VideoReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoReportRepository extends JpaRepository<VideoReport, String> {

    List<VideoReport> findByBasketplanGameGameNumberAndCoachId(String gameNumber, Long id);

    @Query("""
            SELECT v FROM VideoReport v
            WHERE v.basketplanGame.date >= ?1 and v.basketplanGame.date <= ?2
            ORDER BY v.basketplanGame.date DESC,v.basketplanGame.gameNumber DESC, v.reportee DESC
            """)
    List<VideoReport> findAll(LocalDate from, LocalDate to);

    @Query(value = """
            select distinct r.id
            from video_report r
                     join video_report_comment c on c.video_report_id = r.id
                     join login ref on ref.id = case
                                                      when r.reportee = 'FIRST_REFEREE' then r.referee1_id
                                                      when r.reportee = 'SECOND_REFEREE' then r.referee2_id
                                                      else r.referee3_id end
            where r.version > 2
              and r.youtube_id is not null
              and r.finished_at < ?1
              and r.reminder_sent = 0
              and c.requires_reply = 1
            """,
            nativeQuery = true)
    List<String> findReportIdsWithRequiredReplies(LocalDateTime dateTime);

    @Query(value = """
            select vr from VideoReport vr
            where vr.basketplanGame.result like '?%'
              and vr.finished = true
            """)
    List<VideoReport> findReportsWithMissingResult();
}
