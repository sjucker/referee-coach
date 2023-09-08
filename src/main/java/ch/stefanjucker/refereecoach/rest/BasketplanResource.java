package ch.stefanjucker.refereecoach.rest;

import ch.stefanjucker.refereecoach.dto.BasketplanGameDTO;
import ch.stefanjucker.refereecoach.service.BasketplanService;
import ch.stefanjucker.refereecoach.service.BasketplanService.Federation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/game")
public class BasketplanResource {

    private final BasketplanService basketplanService;

    public BasketplanResource(BasketplanService basketplanService) {
        this.basketplanService = basketplanService;
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

}
