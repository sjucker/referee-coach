package ch.stefanjucker.refereecoach;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Minimal WebAuthn authenticator (ES256, "none" attestation) that produces the same JSON as the browser's
 * {@code PublicKeyCredential.toJSON()}, which is what the frontend sends to the backend.
 */
public class SoftwareAuthenticator {

    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final Pattern CHALLENGE = Pattern.compile("\"challenge\":\"([^\"]+)\"");
    private static final Pattern USER_ID = Pattern.compile("\"user\":\\{[^}]*\"id\":\"([^\"]+)\"");

    // authenticator data flags
    private static final int USER_PRESENT = 0x01;
    private static final int USER_VERIFIED = 0x04;
    private static final int ATTESTED_CREDENTIAL_DATA = 0x40;

    private final String rpId;
    private final String origin;
    private final KeyPair keyPair;
    private final byte[] credentialId = new byte[16];
    private byte[] userHandle;
    private int signatureCount;

    public SoftwareAuthenticator(String rpId, String origin) {
        this.rpId = rpId;
        this.origin = origin;
        try {
            var generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            this.keyPair = generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
        new SecureRandom().nextBytes(credentialId);
    }

    /**
     * Answers {@code navigator.credentials.create()} for the given options (as returned by the backend).
     */
    public String register(String optionsJson) {
        var challenge = extract(CHALLENGE, optionsJson);
        userHandle = BASE64_URL_DECODER.decode(extract(USER_ID, optionsJson));

        var clientDataJson = clientDataJson("webauthn.create", challenge);
        var authenticatorData = concat(rpIdHash(),
                                       new byte[]{(byte) (USER_PRESENT | USER_VERIFIED | ATTESTED_CREDENTIAL_DATA)},
                                       ByteBuffer.allocate(4).putInt(signatureCount).array(),
                                       new byte[16], // AAGUID
                                       ByteBuffer.allocate(2).putShort((short) credentialId.length).array(),
                                       credentialId,
                                       coseKey());
        var attestationObject = concat(cborMapHeader(3),
                                       cborText("fmt"), cborText("none"),
                                       cborText("attStmt"), cborMapHeader(0),
                                       cborText("authData"), cborBytes(authenticatorData));

        return """
                {
                  "id": "%1$s",
                  "rawId": "%1$s",
                  "type": "public-key",
                  "authenticatorAttachment": "platform",
                  "response": {
                    "clientDataJSON": "%2$s",
                    "attestationObject": "%3$s",
                    "authenticatorData": "%4$s",
                    "publicKey": "%5$s",
                    "publicKeyAlgorithm": -7,
                    "transports": ["internal"]
                  },
                  "clientExtensionResults": {}
                }
                """.formatted(b64(credentialId), b64(clientDataJson), b64(attestationObject), b64(authenticatorData),
                              b64(keyPair.getPublic().getEncoded()));
    }

    /**
     * Answers {@code navigator.credentials.get()} for the given options (as returned by the backend).
     */
    public String login(String optionsJson) {
        return login(optionsJson, signatureCount + 1);
    }

    public String login(String optionsJson, int newSignatureCount) {
        signatureCount = newSignatureCount;
        var clientDataJson = clientDataJson("webauthn.get", extract(CHALLENGE, optionsJson));
        var authenticatorData = concat(rpIdHash(),
                                       new byte[]{(byte) (USER_PRESENT | USER_VERIFIED)},
                                       ByteBuffer.allocate(4).putInt(signatureCount).array());
        var signature = sign(concat(authenticatorData, sha256(clientDataJson)));

        return """
                {
                  "id": "%1$s",
                  "rawId": "%1$s",
                  "type": "public-key",
                  "authenticatorAttachment": "platform",
                  "response": {
                    "clientDataJSON": "%2$s",
                    "authenticatorData": "%3$s",
                    "signature": "%4$s",
                    "userHandle": "%5$s"
                  },
                  "clientExtensionResults": {}
                }
                """.formatted(b64(credentialId), b64(clientDataJson), b64(authenticatorData), b64(signature), b64(userHandle));
    }

    public byte[] getCredentialId() {
        return credentialId.clone();
    }

    private byte[] clientDataJson(String type, String challenge) {
        return "{\"type\":\"%s\",\"challenge\":\"%s\",\"origin\":\"%s\",\"crossOrigin\":false}"
                .formatted(type, challenge, origin)
                .getBytes(UTF_8);
    }

    private byte[] rpIdHash() {
        return sha256(rpId.getBytes(UTF_8));
    }

    /**
     * EC2 public key in COSE format: {1: 2 (EC2), 3: -7 (ES256), -1: 1 (P-256), -2: x, -3: y}
     */
    private byte[] coseKey() {
        var point = ((ECPublicKey) keyPair.getPublic()).getW();
        return concat(cborMapHeader(5),
                      new byte[]{0x01, 0x02},
                      new byte[]{0x03, 0x26},
                      new byte[]{0x20, 0x01},
                      new byte[]{0x21}, cborBytes(unsigned32(point.getAffineX())),
                      new byte[]{0x22}, cborBytes(unsigned32(point.getAffineY())));
    }

    private byte[] sign(byte[] data) {
        try {
            var signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(data);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String extract(Pattern pattern, String json) {
        var matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("pattern %s not found in %s".formatted(pattern, json));
        }
        return matcher.group(1);
    }

    private static byte[] unsigned32(BigInteger value) {
        var bytes = value.toByteArray();
        var result = new byte[32];
        var length = Math.min(bytes.length, 32);
        System.arraycopy(bytes, bytes.length - length, result, 32 - length, length);
        return result;
    }

    private static byte[] cborMapHeader(int size) {
        return new byte[]{(byte) (0xA0 | size)};
    }

    private static byte[] cborText(String text) {
        var bytes = text.getBytes(UTF_8);
        return concat(new byte[]{(byte) (0x60 | bytes.length)}, bytes);
    }

    private static byte[] cborBytes(byte[] bytes) {
        if (bytes.length < 24) {
            return concat(new byte[]{(byte) (0x40 | bytes.length)}, bytes);
        } else if (bytes.length < 256) {
            return concat(new byte[]{0x58, (byte) bytes.length}, bytes);
        }
        return concat(new byte[]{0x59, (byte) (bytes.length >> 8), (byte) bytes.length}, bytes);
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] concat(byte[]... parts) {
        var out = new ByteArrayOutputStream();
        Arrays.stream(parts).forEach(out::writeBytes);
        return out.toByteArray();
    }

    private static String b64(byte[] bytes) {
        return BASE64_URL.encodeToString(bytes);
    }
}
