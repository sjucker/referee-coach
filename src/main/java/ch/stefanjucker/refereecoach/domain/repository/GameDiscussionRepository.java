package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.GameDiscussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameDiscussionRepository extends JpaRepository<GameDiscussion, String> {

    Optional<GameDiscussion> findByBasketplanGameGameNumber(String gameNumber);

    @Query("""
            SELECT g FROM GameDiscussion g
            WHERE g.basketplanGame.date >= ?1 and g.basketplanGame.date <= ?2
            ORDER BY g.basketplanGame.date DESC, g.basketplanGame.gameNumber DESC
            """)
    List<GameDiscussion> findAll(LocalDate from, LocalDate to);
}
