package ch.stefanjucker.refereecoach.rest;

import ch.stefanjucker.refereecoach.dto.BasketplanGameDTO;
import ch.stefanjucker.refereecoach.service.BasketplanService;
import ch.stefanjucker.refereecoach.service.BasketplanService.Federation;
import ch.stefanjucker.refereecoach.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/game")
public class BasketplanResource {

    private final BasketplanService basketplanService;
    private final LoginService loginService;

    public BasketplanResource(BasketplanService basketplanService,
                              LoginService loginService) {
        this.basketplanService = basketplanService;
        this.loginService = loginService;
    }

    @GetMapping("/{federation}/{gameNumber}")
    @Secured({"COACH"})
    public ResponseEntity<BasketplanGameDTO> game(@PathVariable Federation federation, @PathVariable String gameNumber) {
        log.info("GET /game/{}/{}", federation.getId(), gameNumber);

        try {
            return ResponseEntity.of(basketplanService.findGameByNumber(federation, gameNumber));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{federation}/{gameNumber}/referee")
    @Secured({"REFEREE"})
    public ResponseEntity<BasketplanGameDTO> gameForReferee(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable Federation federation, @PathVariable String gameNumber) {

        var user = loginService.find(principal.getUsername()).orElseThrow();
        if (!user.isReferee()) {
            throw new IllegalStateException("non-referee user should not call this endpoint: %s".formatted(user));
        }

        log.info("GET /game/{}/{}/referee", federation.getId(), gameNumber);

        try {
            var game = basketplanService.findGameByNumber(federation, gameNumber);

            // check that referee did participate in this game and that there is a youtube-link
            if (game.map(dto -> dto.containsReferee(user.getId())).orElse(false)) {
                if (game.map(dto -> StringUtils.isNotBlank(dto.youtubeId())).orElse(false)) {
                    return ResponseEntity.of(game);
                } else {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("gameForReferee failed unexpectedly", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
