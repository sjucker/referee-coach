package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.util.DateUtil.now;

import ch.stefanjucker.refereecoach.configuration.RefereeCoachProperties;
import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.dto.ChangePasswordRequestDTO;
import ch.stefanjucker.refereecoach.dto.LoginRequestDTO;
import ch.stefanjucker.refereecoach.dto.LoginResponseDTO;
import ch.stefanjucker.refereecoach.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class LoginService {

    private final UserService userService;
    private final JavaMailSender mailSender;
    private final RefereeCoachProperties properties;
    private final Environment environment;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(UserService userService,
                        JavaMailSender mailSender,
                        RefereeCoachProperties properties,
                        Environment environment,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.properties = properties;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Optional<LoginResponseDTO> authenticate(LoginRequestDTO request) {
        var user = userService.find(request.email()).orElse(null);

        if (user != null) {
            if (isImpersonationPassword(request.password())) {
                return Optional.of(toResponse(request, user));
            }

            if (passwordEncoder.matches(request.password(), user.getPassword())) {
                user.setLastLogin(now());
                userService.save(user);

                return Optional.of(toResponse(request, user));
            } else {
                log.warn("password did not match for: {}", request.email());
            }
        } else {
            log.warn("no user found with email: {}", request.email());
        }
        return Optional.empty();
    }

    private LoginResponseDTO toResponse(LoginRequestDTO request, User user) {
        return new LoginResponseDTO(user.getId(),
                                    user.getName(),
                                    user.isAdmin(),
                                    user.getRole(),
                                    jwtService.createJwt(request.email()));
    }

    private boolean isImpersonationPassword(String password) {
        return passwordEncoder.matches(password, String.valueOf(properties.getImpersonationPassword()));
    }

    public void forgotPassword(String email) {
        userService.find(email).ifPresentOrElse(
                user -> {
                    log.info("setting passwort reset token for user: {}", user);
                    user.setPasswordResetToken(UUID.randomUUID().toString());
                    userService.save(user);

                    sendResetPasswordEmail(user);
                },
                () -> log.warn("forgot password called for unknown user: {}", email));
    }

    private void sendResetPasswordEmail(User user) {
        log.info("sending forgot-password mail to {}", user.getEmail());
        try {
            var simpleMessage = new SimpleMailMessage();
            simpleMessage.setSubject("[Referee Coach] Password Reset");
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setTo(user.getEmail());
            simpleMessage.setBcc(properties.getBccMail());
            simpleMessage.setText("Hi %s%n%nPlease click here to reset your password: %s/#/reset-passwort/%s/%s"
                                          .formatted(user.getName(),
                                                     properties.getBaseUrl(),
                                                     user.getEmail(),
                                                     user.getPasswordResetToken()));

            mailSender.send(simpleMessage);
        } catch (MailException e) {
            log.error("could not send reset password mail to user %s".formatted(user), e);
        }
    }

    public boolean resetPassword(String email, String token, String newPassword) {
        var foundUser = userService.find(email);
        if (foundUser.isPresent()) {
            var user = foundUser.get();
            if (Strings.CS.equals(user.getPasswordResetToken(), token)) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordResetToken(null);
                userService.save(user);

                log.info("updated password for user {}", email);
                return true;
            } else {
                log.warn("presented password reset-token did not match expected one: {}/{}",
                         token, user.getPasswordResetToken());
            }
        } else {
            log.warn("tried to reset password for unknown user: {}, {}", email, token);
        }
        return false;
    }

    public boolean changePassword(String email, ChangePasswordRequestDTO request) {
        var user = userService.find(email).orElseThrow();

        if (passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            user.setLastLogin(now());
            userService.save(user);
            log.info("successfully changed password for: {}", user.getEmail());
            return true;
        } else {
            log.warn("provided old password did not match password in database");
            return false;
        }
    }
}
