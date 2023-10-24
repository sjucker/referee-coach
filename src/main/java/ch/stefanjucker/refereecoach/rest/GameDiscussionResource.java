package ch.stefanjucker.refereecoach.rest;

import ch.stefanjucker.refereecoach.dto.CreateGameDiscussionCommentDTO;
import ch.stefanjucker.refereecoach.dto.CreateGameDiscussionDTO;
import ch.stefanjucker.refereecoach.dto.GameDiscussionDTO;
import ch.stefanjucker.refereecoach.dto.VideoCommentReplyDTO;
import ch.stefanjucker.refereecoach.service.GameDiscussionService;
import ch.stefanjucker.refereecoach.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/game-discussion")
public class GameDiscussionResource {

    private final LoginService loginService;
    private final GameDiscussionService gameDiscussionService;

    public GameDiscussionResource(LoginService loginService,
                                  GameDiscussionService gameDiscussionService) {
        this.loginService = loginService;
        this.gameDiscussionService = gameDiscussionService;
    }

    @PostMapping
    @Secured({"REFEREE"})
    public ResponseEntity<GameDiscussionDTO> createGameDiscussion(@AuthenticationPrincipal UserDetails principal,
                                                                  @RequestBody @Valid CreateGameDiscussionDTO dto) {
        var referee = loginService.find(principal.getUsername()).orElseThrow();
        log.info("POST /api/game-discussion {} ({})", dto, referee);

        try {
            return ResponseEntity.ok(gameDiscussionService.create(dto.federation(), dto.gameNumber(), dto.youtubeId(), referee));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping(path = "/{id}")
    @Secured({"COACH", "REFEREE"})
    public ResponseEntity<GameDiscussionDTO> getGameDiscussion(@AuthenticationPrincipal UserDetails principal,
                                                               @PathVariable String id) {
        var user = loginService.find(principal.getUsername()).orElseThrow();
        log.info("GET /api/game-discussion/{} ({})", id, user);

        return ResponseEntity.of(gameDiscussionService.get(id));
    }

    @PostMapping(path = "/{id}/comment")
    @Secured({"COACH", "REFEREE"})
    public ResponseEntity<VideoCommentReplyDTO> postComment(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable String id,
                                                            @RequestBody @Valid CreateGameDiscussionCommentDTO dto) {

        var user = loginService.find(principal.getUsername()).orElseThrow();
        log.info("POST /api/game-discussion//{}/comment {} {}", id, dto, user);

        gameDiscussionService.addComments(id, dto, user);

        return ResponseEntity.ok().build();
    }
}
