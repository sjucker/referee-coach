package ch.stefanjucker.refereecoach.security;

import ch.stefanjucker.refereecoach.domain.PasskeyCredential;
import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.PasskeyCredentialRepository;
import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Makes the stored passkeys available to the Yubico WebAuthn library (username = email).
 */
@Component
public class PasskeyCredentialRepositoryAdapter implements CredentialRepository {

    private final PasskeyCredentialRepository passkeyCredentialRepository;
    private final UserRepository userRepository;

    public PasskeyCredentialRepositoryAdapter(PasskeyCredentialRepository passkeyCredentialRepository,
                                              UserRepository userRepository) {
        this.passkeyCredentialRepository = passkeyCredentialRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return passkeyCredentialRepository.findByUserEmail(username).stream()
                                          .map(credential -> PublicKeyCredentialDescriptor.builder()
                                                                                          .id(new ByteArray(credential.getCredentialId()))
                                                                                          .build())
                                          .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByEmail(username)
                             .map(User::getUserHandle)
                             .map(ByteArray::new);
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return userRepository.findByUserHandle(userHandle.getBytes())
                             .map(User::getEmail);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return passkeyCredentialRepository.findByCredentialId(credentialId.getBytes())
                                          .filter(credential -> Arrays.equals(credential.getUser().getUserHandle(), userHandle.getBytes()))
                                          .map(PasskeyCredentialRepositoryAdapter::toRegisteredCredential);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return passkeyCredentialRepository.findByCredentialId(credentialId.getBytes())
                                          .map(PasskeyCredentialRepositoryAdapter::toRegisteredCredential)
                                          .stream()
                                          .collect(Collectors.toSet());
    }

    private static RegisteredCredential toRegisteredCredential(PasskeyCredential credential) {
        return RegisteredCredential.builder()
                                   .credentialId(new ByteArray(credential.getCredentialId()))
                                   .userHandle(new ByteArray(credential.getUser().getUserHandle()))
                                   .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                                   .signatureCount(credential.getSignatureCount())
                                   .build();
    }
}
