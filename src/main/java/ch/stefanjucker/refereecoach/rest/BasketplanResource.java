package ch.stefanjucker.refereecoach.rest;

import ch.stefanjucker.refereecoach.dto.BasketplanGameDTO;
import ch.stefanjucker.refereecoach.service.BasketplanService;
import ch.stefanjucker.refereecoach.service.UserService;
import lombok.extern.slf4j.Slf4j;
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
    private final UserService userService;

    public BasketplanResource(BasketplanService basketplanService,
                              UserService userService) {
        this.basketplanService = basketplanService;
        this.userService = userService;
    }

    @GetMapping("/{gameNumber}")
    @Secured({"COACH", "REFEREE_COACH"})
    public ResponseEntity<BasketplanGameDTO> game(@PathVariable String gameNumber) {
        log.info("GET /game/{}", gameNumber);

        try {
            return ResponseEntity.of(basketplanService.findGameByNumber(gameNumber));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{gameNumber}/referee")
    @Secured({"REFEREE", "REFEREE_COACH"})
    public ResponseEntity<BasketplanGameDTO> gameForReferee(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable String gameNumber) {

        var user = userService.find(principal.getUsername()).orElseThrow();
        if (!user.isReferee()) {
            throw new IllegalStateException("non-referee user should not call this endpoint: %s".formatted(user));
        }

        log.info("GET /game/{}/referee", gameNumber);

        try {
            var game = basketplanService.findGameByNumber(gameNumber);

            // check that referee did participate in this game
            if (game.map(dto -> dto.containsReferee(user.getId())).orElse(false)) {
                return ResponseEntity.of(game);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("gameForReferee failed unexpectedly", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
