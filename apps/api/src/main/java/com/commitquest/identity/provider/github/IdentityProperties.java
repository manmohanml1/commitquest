package com.commitquest.identity.provider.github;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("commitquest.identity")
public record IdentityProperties(
        boolean enabled,
        String clientId,
        String clientSecret,
        URI publicBaseUrl,
        URI authorizationUrl,
        URI tokenUrl,
        URI apiBaseUrl,
        String hmacSecret,
        Duration connectTimeout,
        Duration readTimeout) {

    public void requireCompleteConfiguration() {
        requireText(clientId, "GitHub OAuth client ID");
        requireText(clientSecret, "GitHub OAuth client secret");
        requirePublicBaseUrl(publicBaseUrl);
        requireHttps(authorizationUrl, "GitHub authorization URL");
        requireHttps(tokenUrl, "GitHub token URL");
        requireHttps(apiBaseUrl, "GitHub API base URL");
        if (connectTimeout == null || connectTimeout.isNegative() || connectTimeout.isZero()) {
            throw new IllegalStateException("Identity connect timeout must be positive.");
        }
        if (readTimeout == null || readTimeout.isNegative() || readTimeout.isZero()) {
            throw new IllegalStateException("Identity read timeout must be positive.");
        }
        secretBytes();
    }

    public URI redirectUri() {
        return publicBaseUrl.resolve("/api/v1/auth/github/callback");
    }

    public byte[] secretBytes() {
        requireText(hmacSecret, "Identity HMAC secret");
        try {
            var decoded = Base64.getDecoder().decode(hmacSecret);
            if (decoded.length < 32) throw new IllegalStateException("Identity HMAC secret must decode to 32 bytes.");
            return decoded;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Identity HMAC secret must be Base64 encoded.", exception);
        }
    }

    private static void requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalStateException(label + " is required.");
    }

    private static void requireHttps(URI value, String label) {
        if (value == null || !"https".equalsIgnoreCase(value.getScheme()) || value.getHost() == null) {
            throw new IllegalStateException(label + " must be an absolute HTTPS URL.");
        }
    }

    private static void requirePublicBaseUrl(URI value) {
        if (value == null || value.getHost() == null) {
            throw new IllegalStateException("CommitQuest public base URL must be absolute.");
        }
        var https = "https".equalsIgnoreCase(value.getScheme());
        var localHttp = "http".equalsIgnoreCase(value.getScheme())
                && ("localhost".equalsIgnoreCase(value.getHost()) || "127.0.0.1".equals(value.getHost()));
        if (!https && !localHttp) {
            throw new IllegalStateException("CommitQuest public base URL must use HTTPS except on loopback.");
        }
    }
}
