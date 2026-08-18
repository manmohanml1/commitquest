package com.commitquest.identity.provider.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class IdentityPropertiesTest {

    @Test
    void acceptsHttpsProductionAndLoopbackDevelopmentCallbacks() {
        assertThatCode(() -> properties(URI.create("https://commitquest.example")).requireCompleteConfiguration())
                .doesNotThrowAnyException();
        var local = properties(URI.create("http://localhost:4200"));
        assertThatCode(local::requireCompleteConfiguration).doesNotThrowAnyException();
        assertThat(local.redirectUri()).isEqualTo(URI.create("http://localhost:4200/api/v1/auth/github/callback"));
    }

    @Test
    void rejectsIncompleteSecretsAndNonLoopbackHttp() {
        assertThatThrownBy(() -> properties(URI.create("http://commitquest.example")).requireCompleteConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTPS");
        var shortSecret = new IdentityProperties(
                true,
                "client",
                "secret",
                URI.create("https://commitquest.example"),
                URI.create("https://github.com/login/oauth/authorize"),
                URI.create("https://github.com/login/oauth/access_token"),
                URI.create("https://api.github.com"),
                Base64.getEncoder().encodeToString(new byte[16]),
                Duration.ofSeconds(3),
                Duration.ofSeconds(8));
        assertThatThrownBy(shortSecret::requireCompleteConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    private static IdentityProperties properties(URI publicBaseUrl) {
        return new IdentityProperties(
                true,
                "client",
                "secret",
                publicBaseUrl,
                URI.create("https://github.com/login/oauth/authorize"),
                URI.create("https://github.com/login/oauth/access_token"),
                URI.create("https://api.github.com"),
                Base64.getEncoder().encodeToString(new byte[32]),
                Duration.ofSeconds(3),
                Duration.ofSeconds(8));
    }
}
