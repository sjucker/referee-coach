package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.GameDiscussionComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameDiscussionCommentRepository extends JpaRepository<GameDiscussionComment, Long> {
    List<GameDiscussionComment> findByGameDiscussionId(String gameDiscussionId);
}
