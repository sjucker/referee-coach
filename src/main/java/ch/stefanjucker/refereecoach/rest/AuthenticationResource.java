package ch.stefanjucker.refereecoach.rest;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import ch.stefanjucker.refereecoach.dto.ChangePasswordRequestDTO;
import ch.stefanjucker.refereecoach.dto.ForgotPasswordRequestDTO;
import ch.stefanjucker.refereecoach.dto.LoginRequestDTO;
import ch.stefanjucker.refereecoach.dto.LoginResponseDTO;
import ch.stefanjucker.refereecoach.dto.ResetPasswordRequestDTO;
import ch.stefanjucker.refereecoach.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/authenticate")
public class AuthenticationResource {

    private final LoginService loginService;

    public AuthenticationResource(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        log.info("POST /api/authenticate {}", request.email());

        return loginService.authenticate(request)
                           .map(ResponseEntity::ok)
                           .orElse(ResponseEntity.status(UNAUTHORIZED).build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        log.info("POST POST /api/authenticate/forgot-password {}", request.email());

        loginService.forgotPassword(request.email());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        log.info("POST /api/authenticate/reset-password {} {}", request.email(), request.token());

        var success = loginService.resetPassword(request.email(), request.token(), request.newPassword());

        return success ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails principal,
                                               @RequestBody @Valid ChangePasswordRequestDTO request) {
        log.info("POST /api/authenticate/change-password {}", principal.getUsername());

        var success = loginService.changePassword(principal.getUsername(), request);

        return success ?
                ResponseEntity.accepted().build() :
                ResponseEntity.badRequest().build();
    }
}
