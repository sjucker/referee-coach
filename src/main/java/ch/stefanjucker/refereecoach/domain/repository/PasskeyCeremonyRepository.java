package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.PasskeyCeremony;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface PasskeyCeremonyRepository extends JpaRepository<PasskeyCeremony, String> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PasskeyCeremony c WHERE c.createdAt < :threshold")
    int deleteOlderThan(LocalDateTime threshold);

}
