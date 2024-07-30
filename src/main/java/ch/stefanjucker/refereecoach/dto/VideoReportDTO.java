package ch.stefanjucker.refereecoach.dto;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VideoReportDTO(@NotNull String id,
                             @NotNull BasketplanGameDTO basketplanGame,
                             @NotNull UserDTO coach,
                             @NotNull Reportee reportee,
                             @NotNull CriteriaEvaluationDTO general,
                             @NotNull CriteriaEvaluationDTO image,
                             @NotNull CriteriaEvaluationDTO fitness,
                             @NotNull CriteriaEvaluationDTO mechanics,
                             @NotNull CriteriaEvaluationDTO fouls,
                             @NotNull CriteriaEvaluationDTO violations,
                             @NotNull CriteriaEvaluationDTO gameManagement,
                             @Length(max = 1024) String pointsToKeepComment,
                             @Length(max = 1024) String pointsToImproveComment,
                             @NotNull List<VideoCommentDTO> videoComments,
                             @NotNull List<Reportee> otherReportees,
                             boolean finished,
                             int version) {
    public boolean isTextOnly() {
        return StringUtils.isBlank(basketplanGame().youtubeId());
    }
}
