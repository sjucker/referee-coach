package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.VideoReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
}
