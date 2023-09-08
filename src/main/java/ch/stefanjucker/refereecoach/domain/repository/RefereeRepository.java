package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.Referee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefereeRepository extends JpaRepository<Referee, Long> {

    Optional<Referee> findByName(String name);

    Optional<Referee> findByEmail(String email);
}
