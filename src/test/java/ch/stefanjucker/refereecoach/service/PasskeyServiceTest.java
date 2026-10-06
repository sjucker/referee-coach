package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.util.DateUtil.now;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import ch.stefanjucker.refereecoach.AbstractIntegrationTest;
import ch.stefanjucker.refereecoach.SoftwareAuthenticator;
import ch.stefanjucker.refereecoach.domain.PasskeyCredential;
import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.PasskeyCeremonyRepository;
import ch.stefanjucker.refereecoach.domain.repository.PasskeyCredentialRepository;
import ch.stefanjucker.refereecoach.dto.PasskeyDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

class PasskeyServiceTest extends AbstractIntegrationTest {

    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final String RP_ID = "app.referee-coach.ch";
    private static final String ORIGIN = "https://app.referee-coach.ch";

    @Autowired
    private PasskeyService passkeyService;
    @Autowired
    private PasskeyCredentialRepository passkeyCredentialRepository;
    @Autowired
    private PasskeyCeremonyRepository passkeyCeremonyRepository;

    @AfterEach
    void deletePasskeys() {
        passkeyCredentialRepository.deleteAll();
        passkeyCeremonyRepository.deleteAll();
    }

    @Test
    void registerAndLogin() {
        // given
        var authenticator = new SoftwareAuthenticator(RP_ID, ORIGIN);

        // when
        var passkey = register(authenticator, coach1);

        // then
        assertThat(passkey.name()).isEqualTo("Laptop");
        assertThat(passkey.lastUsedAt()).isNull();
        assertThat(passkeyCredentialRepository.findByCredentialId(authenticator.getCredentialId())).isPresent();

        // when
        var options = passkeyService.startLogin();
        var response = passkeyService.finishLogin(options.ceremonyId(), authenticator.login(options.optionsJson()));

        // then
        assertThat(response).hasValueSatisfying(dto -> {
            assertThat(dto.id()).isEqualTo(coach1.getId());
            assertThat(dto.name()).isEqualTo(coach1.getName());
            assertThat(dto.role()).isEqualTo(coach1.getRole());
            assertThat(dto.jwt()).isNotBlank();
        });
        var credential = passkeyCredentialRepository.findByCredentialId(authenticator.getCredentialId()).orElseThrow();
        assertThat(credential.getSignatureCount()).isEqualTo(1);
        assertThat(credential.getLastUsedAt()).isNotNull();
        assertThat(userRepository.findById(coach1.getId()).orElseThrow().getLastLogin()).isNotNull();
        assertThat(passkeyCeremonyRepository.findById(options.ceremonyId())).isEmpty();
    }

    @Test
    void registerTwoUsers_loginIdentifiesCorrectUser() {
        // given
        var authenticator1 = new SoftwareAuthenticator(RP_ID, ORIGIN);
        var authenticator2 = new SoftwareAuthenticator(RP_ID, ORIGIN);
        register(authenticator1, coach1);
        register(authenticator2, referee1);

        // when
        var options = passkeyService.startLogin();
        var response = passkeyService.finishLogin(options.ceremonyId(), authenticator2.login(options.optionsJson()));

        // then
        assertThat(response).hasValueSatisfying(dto -> assertThat(dto.id()).isEqualTo(referee1.getId()));
    }

    @Test
    void register_wrongOrigin() {
        // given
        var authenticator = new SoftwareAuthenticator(RP_ID, "https://evil.example.com");
        var options = passkeyService.startRegistration(coach1.getEmail());

        // when / then
        assertThatThrownBy(() -> passkeyService.finishRegistration(coach1.getEmail(), options.ceremonyId(),
                                                                   authenticator.register(options.optionsJson()), "Laptop"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST));
        assertThat(passkeyService.list(coach1.getEmail())).isEmpty();
    }

    @Test
    void login_wrongRpId() {
        // given
        var authenticator = new SoftwareAuthenticator("evil.example.com", ORIGIN);
        var options = passkeyService.startRegistration(coach1.getEmail());

        // when / then
        assertThatThrownBy(() -> passkeyService.finishRegistration(coach1.getEmail(), options.ceremonyId(),
                                                                   authenticator.register(options.optionsJson()), "Laptop"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void login_ceremonyCannotBeReused() {
        // given
        var authenticator = new SoftwareAuthenticator(RP_ID, ORIGIN);
        register(authenticator, coach1);
        var options = passkeyService.startLogin();
        var assertion = authenticator.login(options.optionsJson());
        assertThat(passkeyService.finishLogin(options.ceremonyId(), assertion)).isPresent();

        // when / then
        assertThat(passkeyService.finishLogin(options.ceremonyId(), assertion)).isEmpty();
    }

    @Test
    void login_challengeOfOtherCeremony() {
        // given
        var authenticator = new SoftwareAuthenticator(RP_ID, ORIGIN);
        register(authenticator, coach1);
        var options1 = passkeyService.startLogin();
        var options2 = passkeyService.startLogin();

        // when / then
        assertThat(passkeyService.finishLogin(options2.ceremonyId(), authenticator.login(options1.optionsJson()))).isEmpty();
    }

    @Test
    void login_signatureCounterDecreased() {
        // given (e.g., a cloned authenticator)
        var authenticator = new SoftwareAuthenticator(RP_ID, ORIGIN);
        register(authenticator, coach1);
        var options1 = passkeyService.startLogin();
        assertThat(passkeyService.finishLogin(options1.ceremonyId(), authenticator.login(options1.optionsJson(), 5))).isPresent();

        // when
        var options2 = passkeyService.startLogin();
        var response = passkeyService.finishLogin(options2.ceremonyId(), authenticator.login(options2.optionsJson(), 3));

        // then
        assertThat(response).isEmpty();
    }

    @Test
    void login_deletedPasskey() {
        // given
        var authenticator = new SoftwareAuthenticator(RP_ID, ORIGIN);
        var passkey = register(authenticator, coach1);
        passkeyService.delete(coach1.getEmail(), passkey.id());

        // when
        var options = passkeyService.startLogin();
        var response = passkeyService.finishLogin(options.ceremonyId(), authenticator.login(options.optionsJson()));

        // then
        assertThat(response).isEmpty();
    }

    @Test
    void startRegistration() {
        // when
        var result = passkeyService.startRegistration(coach1.getEmail());

        // then
        assertThat(result.ceremonyId()).isNotBlank();
        assertThat(result.optionsJson()).contains("\"id\":\"app.referee-coach.ch\"")
                                        .contains("\"residentKey\":\"required\"");
        assertThat(userRepository.findById(coach1.getId()).orElseThrow().getUserHandle()).hasSize(32);
        assertThat(passkeyCeremonyRepository.findById(result.ceremonyId())).isPresent();
    }

    @Test
    void startRegistration_excludesExistingCredentials() {
        // given
        passkeyService.startRegistration(coach1.getEmail());
        saveCredential(userRepository.findById(coach1.getId()).orElseThrow(), new byte[]{1, 2, 3, 4});

        // when
        var result = passkeyService.startRegistration(coach1.getEmail());

        // then
        assertThat(result.optionsJson()).contains("\"excludeCredentials\":[{").contains("\"id\":\"AQIDBA\"");
    }

    @Test
    void finishRegistration_unknownCeremony() {
        assertThatThrownBy(() -> passkeyService.finishRegistration(coach1.getEmail(), "unknown", "{}", "name"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST));
    }

    @Test
    void finishRegistration_ceremonyOfOtherUser() {
        // given
        var ceremonyId = passkeyService.startRegistration(coach1.getEmail()).ceremonyId();

        // when / then
        assertThatThrownBy(() -> passkeyService.finishRegistration(coach2.getEmail(), ceremonyId, "{}", "name"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST));
        assertThat(passkeyCeremonyRepository.findById(ceremonyId)).isEmpty();
    }

    @Test
    void finishRegistration_invalidCredential() {
        // given
        var ceremonyId = passkeyService.startRegistration(coach1.getEmail()).ceremonyId();

        // when / then
        assertThatThrownBy(() -> passkeyService.finishRegistration(coach1.getEmail(), ceremonyId, "{}", "name"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(BAD_REQUEST));
        assertThat(passkeyCeremonyRepository.findById(ceremonyId)).isEmpty();
        assertThat(passkeyService.list(coach1.getEmail())).isEmpty();
    }

    @Test
    void startLogin() {
        // when
        var result = passkeyService.startLogin();

        // then
        assertThat(result.optionsJson()).contains("\"rpId\":\"app.referee-coach.ch\"")
                                        .doesNotContain("allowCredentials");
        assertThat(passkeyCeremonyRepository.findById(result.ceremonyId())).isPresent();
    }

    @Test
    void finishLogin_unknownCeremony() {
        assertThat(passkeyService.finishLogin("unknown", assertionJson("challenge"))).isEmpty();
    }

    @Test
    void finishLogin_expiredCeremony() {
        // given
        var ceremonyId = passkeyService.startLogin().ceremonyId();
        var ceremony = passkeyCeremonyRepository.findById(ceremonyId).orElseThrow();
        ceremony.setCreatedAt(now().minusMinutes(10));
        passkeyCeremonyRepository.save(ceremony);

        // when / then
        assertThat(passkeyService.finishLogin(ceremonyId, assertionJson("challenge"))).isEmpty();
        assertThat(passkeyCeremonyRepository.findById(ceremonyId)).isEmpty();
    }

    @Test
    void finishLogin_registrationCeremonyNotAccepted() {
        // given
        var ceremonyId = passkeyService.startRegistration(coach1.getEmail()).ceremonyId();

        // when / then
        assertThat(passkeyService.finishLogin(ceremonyId, assertionJson("challenge"))).isEmpty();
    }

    @Test
    void finishLogin_unknownCredential() {
        // given
        var options = passkeyService.startLogin();
        var challenge = options.optionsJson().replaceAll(".*\"challenge\":\"([^\"]+)\".*", "$1");

        // when / then
        assertThat(passkeyService.finishLogin(options.ceremonyId(), assertionJson(challenge))).isEmpty();
        assertThat(passkeyCeremonyRepository.findById(options.ceremonyId())).isEmpty();
    }

    @Test
    void listRenameDelete() {
        // given
        passkeyService.startRegistration(coach1.getEmail());
        var credential = saveCredential(userRepository.findById(coach1.getId()).orElseThrow(), new byte[]{1, 2, 3, 4});

        // when
        passkeyService.rename(coach1.getEmail(), credential.getId(), " MacBook ");

        // then
        assertThat(passkeyService.list(coach1.getEmail())).singleElement()
                                                          .satisfies(dto -> assertThat(dto.name()).isEqualTo("MacBook"));

        // when
        passkeyService.delete(coach1.getEmail(), credential.getId());

        // then
        assertThat(passkeyService.list(coach1.getEmail())).isEmpty();
    }

    @Test
    void otherUsersPasskeysAreNotAccessible() {
        // given
        passkeyService.startRegistration(coach1.getEmail());
        var credential = saveCredential(userRepository.findById(coach1.getId()).orElseThrow(), new byte[]{1, 2, 3, 4});

        // when / then
        assertThat(passkeyService.list(coach2.getEmail())).isEmpty();
        assertThatThrownBy(() -> passkeyService.rename(coach2.getEmail(), credential.getId(), "mine"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND));
        assertThatThrownBy(() -> passkeyService.delete(coach2.getEmail(), credential.getId()))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(NOT_FOUND));
        assertThat(passkeyCredentialRepository.findById(credential.getId())).isPresent();
    }

    @Test
    void deleteExpiredCeremonies() {
        // given
        var expired = passkeyService.startLogin().ceremonyId();
        var ceremony = passkeyCeremonyRepository.findById(expired).orElseThrow();
        ceremony.setCreatedAt(now().minusMinutes(10));
        passkeyCeremonyRepository.save(ceremony);
        var valid = passkeyService.startLogin().ceremonyId();

        // when
        var deleted = passkeyService.deleteExpiredCeremonies();

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(passkeyCeremonyRepository.findById(expired)).isEmpty();
        assertThat(passkeyCeremonyRepository.findById(valid)).isPresent();
    }

    private PasskeyDTO register(SoftwareAuthenticator authenticator, User user) {
        var options = passkeyService.startRegistration(user.getEmail());
        return passkeyService.finishRegistration(user.getEmail(), options.ceremonyId(), authenticator.register(options.optionsJson()), "Laptop");
    }

    private PasskeyCredential saveCredential(User user, byte[] credentialId) {
        var credential = new PasskeyCredential();
        credential.setUser(user);
        credential.setCredentialId(credentialId);
        credential.setPublicKeyCose(new byte[]{5, 6, 7, 8});
        credential.setSignatureCount(0);
        credential.setName("Passkey");
        credential.setCreatedAt(now());
        return passkeyCredentialRepository.save(credential);
    }

    /**
     * Structurally valid assertion of a credential that is not registered.
     */
    private static String assertionJson(String challenge) {
        var credentialId = BASE64_URL.encodeToString(new byte[]{9, 9, 9, 9});
        var clientData = BASE64_URL.encodeToString(
                "{\"type\":\"webauthn.get\",\"challenge\":\"%s\",\"origin\":\"https://app.referee-coach.ch\"}".formatted(challenge).getBytes(UTF_8));
        // rpIdHash (32 bytes) + flags (1 byte) + signature counter (4 bytes)
        var authenticatorData = BASE64_URL.encodeToString(new byte[37]);
        return """
                {
                  "id": "%s",
                  "rawId": "%s",
                  "type": "public-key",
                  "response": {
                    "clientDataJSON": "%s",
                    "authenticatorData": "%s",
                    "signature": "%s",
                    "userHandle": "%s"
                  },
                  "clientExtensionResults": {}
                }
                """.formatted(credentialId, credentialId, clientData, authenticatorData,
                              BASE64_URL.encodeToString(new byte[]{1}), BASE64_URL.encodeToString(new byte[]{2}));
    }
}
