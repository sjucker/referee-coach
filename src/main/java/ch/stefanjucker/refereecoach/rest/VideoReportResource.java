package ch.stefanjucker.refereecoach.rest;

import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import ch.stefanjucker.refereecoach.dto.CopyVideoReportDTO;
import ch.stefanjucker.refereecoach.dto.CreateVideoReportDTO;
import ch.stefanjucker.refereecoach.dto.VideoReportDTO;
import ch.stefanjucker.refereecoach.service.VideoReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/api/video-report")
public class VideoReportResource {

    private final VideoReportService videoReportService;
    private final UserRepository userRepository;

    public VideoReportResource(VideoReportService videoReportService, UserRepository userRepository) {
        this.videoReportService = videoReportService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<VideoReportDTO>> getAllReports() {
        log.info("GET /video-report");
        return ResponseEntity.ok(videoReportService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoReportDTO> getVideoReport(@PathVariable String id) {
        log.info("GET /video-report/{}", id);

        return videoReportService.find(id)
                                 .map(ResponseEntity::ok)
                                 .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoReportDTO> createVideoReport(@AuthenticationPrincipal UserDetails principal,
                                                            @RequestBody @Valid CreateVideoReportDTO dto) {
        var user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        log.info("POST /video-report {} ({})", dto, user);

        return ResponseEntity.ok(videoReportService.create(dto.federation(), dto.gameNumber(), dto.youtubeId(), dto.reportee(), user));
    }

    @PostMapping(path = "/copy", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoReportDTO> copyVideoReport(@AuthenticationPrincipal UserDetails principal,
                                                          @RequestBody @Valid CopyVideoReportDTO dto) {
        var user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        log.info("POST /video-report/copy {} ({})", dto, user);

        return ResponseEntity.ok(videoReportService.copy(dto.sourceId(), dto.reportee(), user));
    }

    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoReportDTO> updateVideoReport(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable String id,
                                                            @RequestBody @Valid VideoReportDTO dto) {
        var user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        log.info("PUT /video-report/{} {} ({})", id, dto, user);
        return ResponseEntity.ok(videoReportService.update(id, dto, user));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteVideoReport(@AuthenticationPrincipal UserDetails principal,
                                               @PathVariable String id) {
        var user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        log.info("DELETE /video-report/{} ({})", id, user);

        videoReportService.delete(id, user);
        return ResponseEntity.ok().build();
    }

}
