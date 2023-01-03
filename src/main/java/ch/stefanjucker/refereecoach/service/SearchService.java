package ch.stefanjucker.refereecoach.service;

import ch.stefanjucker.refereecoach.dto.SearchRequestDTO;
import ch.stefanjucker.refereecoach.dto.TagDTO;
import ch.stefanjucker.refereecoach.dto.VideoCommentDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class SearchService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SearchService(DataSource dataSource) {
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
}
