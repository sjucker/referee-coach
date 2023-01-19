package ch.stefanjucker.refereecoach.rest;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import ch.stefanjucker.refereecoach.domain.repository.CoachRepository;
import ch.stefanjucker.refereecoach.dto.ChangePasswordRequestDTO;
import ch.stefanjucker.refereecoach.dto.LoginRequestDTO;
import ch.stefanjucker.refereecoach.dto.LoginResponseDTO;
import ch.stefanjucker.refereecoach.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/authenticate")
public class AuthenticationResource {

    private final JwtService jwtService;
    private final CoachRepository coachRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationResource(JwtService jwtService, CoachRepository coachRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.coachRepository = coachRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        log.info("POST /api/authenticate {}", request.email());

        var coach = coachRepository.findByEmail(request.email()).orElse(null);

        if (coach != null) {
            if (passwordEncoder.matches(request.password(), coach.getPassword())) {

                coach.setLastLogin(LocalDateTime.now());
                coachRepository.save(coach);

                return ResponseEntity.ok(new LoginResponseDTO(
                        coach.getId(),
                        coach.getName(),
                        coach.isAdmin(),
                        jwtService.createJwt(request.email())
                ));
            } else {
                log.warn("password did not match for: {}", request.email());
            }
        } else {
            log.warn("no user found with email: {}", request.email());
        }

        return ResponseEntity.status(UNAUTHORIZED).build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails principal,
                                            @RequestBody @Valid ChangePasswordRequestDTO request) {
        var coach = coachRepository.findByEmail(principal.getUsername()).orElseThrow();

        log.info("POST /api/authenticate/change-password {}", coach.getEmail());

        if (passwordEncoder.matches(request.oldPassword(), coach.getPassword())) {
            coach.setPassword(passwordEncoder.encode(request.newPassword()));
            coachRepository.save(coach);
            log.info("successfully changed password for: {}", coach.getEmail());
            return ResponseEntity.accepted().build();
        } else {
            log.warn("provided old password did not match password in database");
        }

        return ResponseEntity.badRequest().build();
    }
}
