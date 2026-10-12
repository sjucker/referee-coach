package ch.stefanjucker.refereecoach.domain.repository;

import ch.stefanjucker.refereecoach.domain.Tags;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagsRepository extends JpaRepository<Tags, Long> {

    @Override
    @Cacheable("tags")
    List<Tags> findAll();

    Optional<Tags> findByNameIgnoreCase(String name);

    @Query(value = """
            SELECT t.id AS id, t.name AS name, COUNT(ct.tag_id) AS usageCount
            FROM tags t
            LEFT JOIN video_report_comment_tags ct ON ct.tag_id = t.id
            GROUP BY t.id, t.name
            ORDER BY t.name
            """, nativeQuery = true)
    List<TagUsage> findAllWithUsageCount();

    @Query(value = "SELECT COUNT(*) FROM video_report_comment_tags WHERE tag_id = :tagId", nativeQuery = true)
    long countUsages(Long tagId);

    interface TagUsage {
        Long getId();

        String getName();

        Long getUsageCount();
    }
}
