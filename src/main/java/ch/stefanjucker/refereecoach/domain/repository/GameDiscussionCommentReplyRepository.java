package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.GameDiscussionCommentReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameDiscussionCommentReplyRepository extends JpaRepository<GameDiscussionCommentReply, Long> {
    List<GameDiscussionCommentReply> findByGameDiscussionCommentIdOrderByRepliedAt(Long gameDiscussionCommentId);
}
