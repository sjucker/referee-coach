package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.PasskeyCredential;
import ch.stefanjucker.refereecoach.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {

    List<PasskeyCredential> findByUserOrderByCreatedAt(User user);

    List<PasskeyCredential> findByUserEmail(String email);

    Optional<PasskeyCredential> findByCredentialId(byte[] credentialId);

}
