package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.dto.OverviewDTO.ReportType.COACHING;
import static ch.stefanjucker.refereecoach.dto.OverviewDTO.ReportType.GAME_DISCUSSION;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import ch.stefanjucker.refereecoach.domain.repository.GameDiscussionRepository;
import ch.stefanjucker.refereecoach.domain.repository.VideoReportRepository;
import ch.stefanjucker.refereecoach.dto.CoachDTO;
import ch.stefanjucker.refereecoach.dto.OverviewDTO;
import ch.stefanjucker.refereecoach.dto.SearchRequestDTO;
import ch.stefanjucker.refereecoach.dto.TagDTO;
import ch.stefanjucker.refereecoach.dto.VideoCommentDetailDTO;
import ch.stefanjucker.refereecoach.mapper.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class SearchService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private final VideoReportRepository videoReportRepository;
    private final GameDiscussionRepository gameDiscussionRepository;
    private final LoginService loginService;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SearchService(VideoReportRepository videoReportRepository,
                         GameDiscussionRepository gameDiscussionRepository,
                         LoginService loginService,
                         DataSource dataSource) {
        this.videoReportRepository = videoReportRepository;
        this.gameDiscussionRepository = gameDiscussionRepository;
        this.loginService = loginService;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<VideoCommentDetailDTO> search(SearchRequestDTO dto) {
        var parameters = new MapSqlParameterSource("ids", dto.tags().stream().map(TagDTO::id).toList());

        return jdbcTemplate.query("""
                                          select r.game_number, r.competition, r.date, c.timestamp, c.comment, r.youtube_id, group_concat(distinct(tag.name)) as tags
                                                                  from video_report_comment c
                                                                           join video_report_comment_tags t on c.id = t.video_report_comment_id
                                                                           join tags tag on t.tag_id = tag.id
                                                                           join video_report r on c.video_report_id = r.id
                                                                  where t.tag_id in (:ids)
                                                                  group by r.game_number, r.competition, r.date, c.timestamp, c.comment, r.youtube_id
                                                                  order by r.date desc
                                           """,
                                  parameters,
                                  (rs, rowNum) -> new VideoCommentDetailDTO(
                                          rs.getString("game_number"),
                                          rs.getString("competition"),
                                          LocalDate.parse(rs.getString("date")),
                                          rs.getInt("timestamp"),
                                          rs.getString("comment"),
                                          rs.getString("youtube_id"),
                                          rs.getString("tags")
                                  ));

    }

    public List<OverviewDTO> findAll(LocalDate from, LocalDate to, String email) {
        var user = loginService.find(email).orElseThrow();
        // TODO improvement: filter already in DB query
        return Stream.concat(getCoachingsStream(from, to), getGameDiscussionsStream(from, to))
                     // coach see everything, referee only own reports
                     .filter(overview -> user.isCoach() || overview.relevantRefereeIds().contains(user.getId()))
                     // referee only see finished reports
                     .filter(overview -> user.isCoach() || overview.isVisibleForReferee())
                     .sorted(comparing(OverviewDTO::date).reversed()
                                                         .thenComparing(OverviewDTO::gameNumber)
                                                         .thenComparing(OverviewDTO::reportee, nullsLast(naturalOrder())))
                     .toList();
    }

    private Stream<OverviewDTO> getCoachingsStream(LocalDate from, LocalDate to) {
        return videoReportRepository.findAll(from, to)
                                    .stream()
                                    .map(videoReport -> new OverviewDTO(
                                            videoReport.getId(),
                                            COACHING,
                                            videoReport.getBasketplanGame().getDate(),
                                            videoReport.getBasketplanGame().getGameNumber(),
                                            videoReport.getBasketplanGame().getCompetition(),
                                            videoReport.getBasketplanGame().getTeamA(),
                                            videoReport.getBasketplanGame().getTeamB(),
                                            new CoachDTO(videoReport.getCoach().getId(), videoReport.getCoach().getName()),
                                            videoReport.getReportee(),
                                            DTO_MAPPER.toDTO(videoReport.relevantReferee()),
                                            DTO_MAPPER.toDTO(videoReport.getBasketplanGame().getReferee1()),
                                            DTO_MAPPER.toDTO(videoReport.getBasketplanGame().getReferee2()),
                                            DTO_MAPPER.toDTO(videoReport.getBasketplanGame().getReferee3()),
                                            videoReport.relevantRefereeIds(),
                                            videoReport.isFinished()
                                    ));
    }

    private Stream<OverviewDTO> getGameDiscussionsStream(LocalDate from, LocalDate to) {
        return gameDiscussionRepository.findAll(from, to)
                                       .stream()
                                       .map(gameDiscussion -> new OverviewDTO(
                                               gameDiscussion.getId(),
                                               GAME_DISCUSSION,
                                               gameDiscussion.getBasketplanGame().getDate(),
                                               gameDiscussion.getBasketplanGame().getGameNumber(),
                                               gameDiscussion.getBasketplanGame().getCompetition(),
                                               gameDiscussion.getBasketplanGame().getTeamA(),
                                               gameDiscussion.getBasketplanGame().getTeamB(),
                                               null,
                                               null,
                                               null,
                                               DTO_MAPPER.toDTO(gameDiscussion.getBasketplanGame().getReferee1()),
                                               DTO_MAPPER.toDTO(gameDiscussion.getBasketplanGame().getReferee2()),
                                               DTO_MAPPER.toDTO(gameDiscussion.getBasketplanGame().getReferee3()),
                                               gameDiscussion.relevantRefereeIds(),
                                               false
                                       ));
    }
}
