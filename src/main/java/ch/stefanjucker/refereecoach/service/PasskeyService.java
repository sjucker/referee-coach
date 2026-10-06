package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.util.DateUtil.now;
import static com.yubico.webauthn.data.ResidentKeyRequirement.REQUIRED;
import static com.yubico.webauthn.data.UserVerificationRequirement.PREFERRED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import ch.stefanjucker.refereecoach.domain.PasskeyCeremony;
import ch.stefanjucker.refereecoach.domain.PasskeyCredential;
import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.PasskeyCeremonyRepository;
import ch.stefanjucker.refereecoach.domain.repository.PasskeyCredentialRepository;
import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import ch.stefanjucker.refereecoach.dto.LoginResponseDTO;
import ch.stefanjucker.refereecoach.dto.PasskeyDTO;
import ch.stefanjucker.refereecoach.dto.PasskeyOptionsDTO;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PasskeyService {

    static final Duration CEREMONY_TIMEOUT = Duration.ofMinutes(5);
    private static final int USER_HANDLE_LENGTH = 32;
    private static final String DEFAULT_NAME = "Passkey";

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final PasskeyCredentialRepository passkeyCredentialRepository;
    private final PasskeyCeremonyRepository passkeyCeremonyRepository;
    private final LoginService loginService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasskeyService(RelyingParty relyingParty,
                          UserRepository userRepository,
                          PasskeyCredentialRepository passkeyCredentialRepository,
                          PasskeyCeremonyRepository passkeyCeremonyRepository,
                          LoginService loginService) {
        this.relyingParty = relyingParty;
        this.userRepository = userRepository;
        this.passkeyCredentialRepository = passkeyCredentialRepository;
        this.passkeyCeremonyRepository = passkeyCeremonyRepository;
        this.loginService = loginService;
    }

    @Transactional
    public PasskeyOptionsDTO startRegistration(String email) {
        var user = userRepository.findByEmail(email).orElseThrow();
        if (user.getUserHandle() == null) {
            var userHandle = new byte[USER_HANDLE_LENGTH];
            secureRandom.nextBytes(userHandle);
            user.setUserHandle(userHandle);
            userRepository.save(user);
        }

        var request = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                                        .user(UserIdentity.builder()
                                                          .name(user.getEmail())
                                                          .displayName(user.getName())
                                                          .id(new ByteArray(user.getUserHandle()))
                                                          .build())
                                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                                                                              .residentKey(REQUIRED)
                                                                                              .userVerification(PREFERRED)
                                                                                              .build())
                                        .build());

        try {
            var ceremony = saveCeremony(request.toJson(), user.getId());
            return new PasskeyOptionsDTO(ceremony.getId(), request.toCredentialsCreateJson());
        } catch (IOException e) {
            throw new IllegalStateException("could not serialize passkey registration request", e);
        }
    }

    // keep the consumed ceremony deleted even if the registration fails
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public PasskeyDTO finishRegistration(String email, String ceremonyId, String credentialJson, String name) {
        var user = userRepository.findByEmail(email).orElseThrow();
        var ceremony = consumeCeremony(ceremonyId)
                .filter(c -> Objects.equals(c.getUserId(), user.getId()))
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "invalid or expired passkey registration"));

        try {
            var request = PublicKeyCredentialCreationOptions.fromJson(ceremony.getRequestJson());
            var response = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
            var result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                                                                                  .request(request)
                                                                                  .response(response)
                                                                                  .build());

            var credential = new PasskeyCredential();
            credential.setUser(user);
            credential.setCredentialId(result.getKeyId().getId().getBytes());
            credential.setPublicKeyCose(result.getPublicKeyCose().getBytes());
            credential.setSignatureCount(result.getSignatureCount());
            credential.setName(StringUtils.isBlank(name) ? DEFAULT_NAME : name.trim());
            credential.setCreatedAt(now());

            log.info("registered passkey for user {}", email);
            return toDTO(passkeyCredentialRepository.save(credential));
        } catch (RegistrationFailedException | IOException e) {
            log.warn("passkey registration failed for user {}", email, e);
            throw new ResponseStatusException(BAD_REQUEST, "passkey registration failed");
        }
    }

    @Transactional
    public PasskeyOptionsDTO startLogin() {
        // no username: the browser offers all discoverable passkeys for this site
        var request = relyingParty.startAssertion(StartAssertionOptions.builder()
                                                                       .userVerification(PREFERRED)
                                                                       .build());
        try {
            var ceremony = saveCeremony(request.toJson(), null);
            return new PasskeyOptionsDTO(ceremony.getId(), request.toCredentialsGetJson());
        } catch (IOException e) {
            throw new IllegalStateException("could not serialize passkey login request", e);
        }
    }

    @Transactional
    public Optional<LoginResponseDTO> finishLogin(String ceremonyId, String credentialJson) {
        var ceremony = consumeCeremony(ceremonyId).filter(c -> c.getUserId() == null);
        if (ceremony.isEmpty()) {
            log.warn("invalid or expired passkey login ceremony: {}", ceremonyId);
            return Optional.empty();
        }

        try {
            var request = AssertionRequest.fromJson(ceremony.get().getRequestJson());
            var response = PublicKeyCredential.parseAssertionResponseJson(credentialJson);
            var result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                                                                            .request(request)
                                                                            .response(response)
                                                                            .build());
            if (!result.isSuccess()) {
                log.warn("passkey login was not successful");
                return Optional.empty();
            }

            var credential = passkeyCredentialRepository.findByCredentialId(result.getCredential().getCredentialId().getBytes())
                                                        .orElseThrow();
            credential.setSignatureCount(result.getSignatureCount());
            credential.setLastUsedAt(now());
            passkeyCredentialRepository.save(credential);

            var user = credential.getUser();
            user.setLastLogin(now());
            userRepository.save(user);

            log.info("passkey login for user {}", user.getEmail());
            return Optional.of(loginService.createLoginResponse(user));
        } catch (AssertionFailedException | IOException e) {
            log.warn("passkey login failed", e);
            return Optional.empty();
        }
    }

    public List<PasskeyDTO> list(String email) {
        var user = userRepository.findByEmail(email).orElseThrow();
        return passkeyCredentialRepository.findByUserOrderByCreatedAt(user).stream()
                                          .map(PasskeyService::toDTO)
                                          .toList();
    }

    @Transactional
    public PasskeyDTO rename(String email, Long id, String name) {
        var credential = findOwnCredential(email, id);
        credential.setName(name.trim());
        return toDTO(passkeyCredentialRepository.save(credential));
    }

    @Transactional
    public void delete(String email, Long id) {
        var credential = findOwnCredential(email, id);
        passkeyCredentialRepository.delete(credential);
        log.info("deleted passkey {} of user {}", id, email);
    }

    @Transactional
    public int deleteExpiredCeremonies() {
        return passkeyCeremonyRepository.deleteOlderThan(now().minus(CEREMONY_TIMEOUT));
    }

    private PasskeyCredential findOwnCredential(String email, Long id) {
        return passkeyCredentialRepository.findById(id)
                                          .filter(credential -> credential.getUser().getEmail().equals(email))
                                          .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "passkey not found: " + id));
    }

    private PasskeyCeremony saveCeremony(String requestJson, Long userId) {
        return passkeyCeremonyRepository.save(new PasskeyCeremony(UUID.randomUUID().toString(), requestJson, userId, now()));
    }

    /**
     * A ceremony can only be used once, so it is deleted in any case.
     */
    private Optional<PasskeyCeremony> consumeCeremony(String ceremonyId) {
        var ceremony = passkeyCeremonyRepository.findById(ceremonyId);
        ceremony.ifPresent(passkeyCeremonyRepository::delete);
        return ceremony.filter(c -> c.getCreatedAt().isAfter(now().minus(CEREMONY_TIMEOUT)));
    }

    private static PasskeyDTO toDTO(PasskeyCredential credential) {
        return new PasskeyDTO(credential.getId(), credential.getName(), credential.getCreatedAt(), credential.getLastUsedAt());
    }
}
