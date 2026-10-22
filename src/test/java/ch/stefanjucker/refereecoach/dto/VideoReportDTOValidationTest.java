package ch.stefanjucker.refereecoach.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;

class VideoReportDTOValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void commentWithoutTagsIsInvalid() {
        var comment = new VideoCommentDTO(null, 42, "comment", false, List.of(), null);

        assertThat(violationPaths(reportWith(comment))).contains("videoComments[0].tags");
    }

    @Test
    void tagWithoutIdIsInvalid() {
        var comment = new VideoCommentDTO(null, 42, "comment", false, List.of(), Set.of(new TagDTO(null, "tag")));

        assertThat(violationPaths(reportWith(comment))).contains("videoComments[0].tags[].id");
    }

    @Test
    void validCommentHasNoViolations() {
        var comment = new VideoCommentDTO(null, 42, "comment", false, List.of(), Set.of(new TagDTO(1L, "tag")));

        assertThat(violationPaths(reportWith(comment))).noneMatch(path -> path.startsWith("videoComments"));
    }

    private List<String> violationPaths(VideoReportDTO dto) {
        return validator.validate(dto).stream()
                        .map(ConstraintViolation::getPropertyPath)
                        .map(Object::toString)
                        .toList();
    }

    // only the comments matter here, violations of the other (null) properties are ignored
    private static VideoReportDTO reportWith(VideoCommentDTO comment) {
        return new VideoReportDTO("id", null, null, null, null, null, null, null, null, null, null, null, null,
                                  List.of(comment), List.of(), false, 0);
    }
}
