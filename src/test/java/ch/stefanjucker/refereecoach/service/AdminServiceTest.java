package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.Fixtures.videoComment;
import static ch.stefanjucker.refereecoach.Fixtures.videoReport;
import static ch.stefanjucker.refereecoach.dto.Reportee.FIRST_REFEREE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.domain.repository.TagsRepository;
import ch.stefanjucker.refereecoach.dto.TagNameDTO;
import ch.stefanjucker.refereecoach.dto.TagUsageDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

class AdminServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AdminService adminService;
    @Autowired
    private TagsRepository tagsRepository;

    private final List<Long> createdTagIds = new ArrayList<>();

    @AfterEach
    void deleteCreatedTags() {
        videoCommentRepository.deleteAll();
        tagsRepository.deleteAllById(createdTagIds);
    }

    @Test
    void createRenameAndDeleteTag() {
        var created = createTag("TEST - NEW TAG");
        assertThat(created.usageCount()).as("usage count of new tag").isZero();
        assertThat(adminService.getAllTags()).as("all tags").extracting(TagUsageDTO::name).contains("TEST - NEW TAG");

        adminService.renameTag(created.id(), new TagNameDTO("  TEST - RENAMED  "));
        assertThat(tagsRepository.findById(created.id())).as("renamed tag").get()
                                                         .extracting("name").isEqualTo("TEST - RENAMED");
        assertThat(tagsRepository.findAll()).as("cached tags are evicted").extracting("name").contains("TEST - RENAMED");

        adminService.deleteTag(created.id());
        assertThat(tagsRepository.findById(created.id())).as("deleted tag").isEmpty();
    }

    @Test
    void createDuplicateTagIsRejected() {
        createTag("TEST - DUPLICATE");

        assertThatThrownBy(() -> adminService.createTag(new TagNameDTO("test - duplicate")))
                .as("duplicate tag")
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void renameToExistingNameIsRejected() {
        createTag("TEST - A");
        var b = createTag("TEST - B");

        assertThatThrownBy(() -> adminService.renameTag(b.id(), new TagNameDTO("TEST - A")))
                .as("rename to existing name")
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void usedTagIsCountedAndCannotBeDeleted() {
        var created = createTag("TEST - USED");
        videoReportRepository.save(videoReport("1", coach1, referee1, referee2, referee3, FIRST_REFEREE));
        var comment = videoComment("1", false);
        comment.getTags().add(tagsRepository.findById(created.id()).orElseThrow());
        videoCommentRepository.save(comment);

        assertThat(adminService.getAllTags()).as("all tags")
                                             .filteredOn(tag -> tag.id().equals(created.id()))
                                             .extracting(TagUsageDTO::usageCount)
                                             .containsExactly(1L);
        assertThatThrownBy(() -> adminService.deleteTag(created.id()))
                .as("delete used tag")
                .isInstanceOf(ResponseStatusException.class);
    }

    private TagUsageDTO createTag(String name) {
        var created = adminService.createTag(new TagNameDTO(name));
        createdTagIds.add(created.id());
        return created;
    }
}
