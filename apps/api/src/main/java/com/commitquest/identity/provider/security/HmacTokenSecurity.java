package com.commitquest.identity.provider.security;

import com.commitquest.identity.application.TokenSecurity;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HmacTokenSecurity implements TokenSecurity {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final byte[] secret;

    public HmacTokenSecurity(byte[] secret) {
        if (secret == null || secret.length < 32) {
            throw new IllegalArgumentException("Identity HMAC secret must contain at least 32 bytes.");
        }
        this.secret = secret.clone();
    }

    @Override
    public String randomToken() {
        var bytes = new byte[32];
        random.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }

    @Override
    public String digest(String token) {
        return hex(hmac("digest:" + token));
    }

    @Override
    public String codeVerifier(String state) {
        return URL_ENCODER.encodeToString(hmac("pkce:" + state));
    }

    @Override
    public String codeChallenge(String codeVerifier) {
        try {
            return URL_ENCODER.encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private byte[] hmac(String value) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HMAC-SHA-256 is unavailable.", exception);
        }
    }

    private static String hex(byte[] value) {
        var result = new char[value.length * 2];
        for (var index = 0; index < value.length; index++) {
            var unsigned = value[index] & 0xff;
            result[index * 2] = HEX[unsigned >>> 4];
            result[index * 2 + 1] = HEX[unsigned & 0x0f];
        }
        return new String(result);
    }
}
