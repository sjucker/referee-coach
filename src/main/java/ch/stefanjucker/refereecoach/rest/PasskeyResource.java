package ch.stefanjucker.refereecoach.rest;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ch.stefanjucker.refereecoach.dto.LoginResponseDTO;
import ch.stefanjucker.refereecoach.dto.PasskeyDTO;
import ch.stefanjucker.refereecoach.dto.PasskeyLoginRequestDTO;
import ch.stefanjucker.refereecoach.dto.PasskeyOptionsDTO;
import ch.stefanjucker.refereecoach.dto.PasskeyRegistrationRequestDTO;
import ch.stefanjucker.refereecoach.dto.RenamePasskeyRequestDTO;
import ch.stefanjucker.refereecoach.service.PasskeyService;
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

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/passkey")
public class PasskeyResource {

    private final PasskeyService passkeyService;

    public PasskeyResource(PasskeyService passkeyService) {
        this.passkeyService = passkeyService;
    }

    @PostMapping(value = "/login/start", produces = APPLICATION_JSON_VALUE)
    public PasskeyOptionsDTO startLogin() {
        log.info("POST /api/passkey/login/start");

        return passkeyService.startLogin();
    }

    @PostMapping(value = "/login/finish", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> finishLogin(@RequestBody @Valid PasskeyLoginRequestDTO request) {
        log.info("POST /api/passkey/login/finish");

        return passkeyService.finishLogin(request.ceremonyId(), request.credentialJson())
                             .map(ResponseEntity::ok)
                             .orElse(ResponseEntity.status(UNAUTHORIZED).build());
    }

    @PostMapping(value = "/register/start", produces = APPLICATION_JSON_VALUE)
    public PasskeyOptionsDTO startRegistration(@AuthenticationPrincipal UserDetails principal) {
        log.info("POST /api/passkey/register/start {}", principal.getUsername());

        return passkeyService.startRegistration(principal.getUsername());
    }

    @PostMapping(value = "/register/finish", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public PasskeyDTO finishRegistration(@AuthenticationPrincipal UserDetails principal,
                                         @RequestBody @Valid PasskeyRegistrationRequestDTO request) {
        log.info("POST /api/passkey/register/finish {}", principal.getUsername());

        return passkeyService.finishRegistration(principal.getUsername(), request.ceremonyId(), request.credentialJson(), request.name());
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<PasskeyDTO> list(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/passkey {}", principal.getUsername());

        return passkeyService.list(principal.getUsername());
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public PasskeyDTO rename(@AuthenticationPrincipal UserDetails principal,
                             @PathVariable Long id,
                             @RequestBody @Valid RenamePasskeyRequestDTO request) {
        log.info("PUT /api/passkey/{} {}", id, principal.getUsername());

        return passkeyService.rename(principal.getUsername(), id, request.name());
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        log.info("DELETE /api/passkey/{} {}", id, principal.getUsername());

        passkeyService.delete(principal.getUsername(), id);
    }
}
