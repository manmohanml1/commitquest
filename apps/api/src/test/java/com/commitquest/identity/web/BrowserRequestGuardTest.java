package com.commitquest.identity.web;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.commitquest.identity.application.IdentityFailure;
import com.commitquest.preview.web.WebProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class BrowserRequestGuardTest {

    private final BrowserRequestGuard guard = new BrowserRequestGuard(new WebProperties(List.of(
            "http://localhost:4200",
            "https://commitquest-web.vercel.app",
            "https://commitquest-web-*.vercel.app")));

    @Test
    void acceptsAllowlistedOriginOrReferer() {
        assertThatCode(() -> guard.requireAllowedSource("http://localhost:4200", null)).doesNotThrowAnyException();
        assertThatCode(() -> guard.requireAllowedSource(
                        null, "https://commitquest-web-feature.vercel.app/campaign?id=1"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingMalformedAndCrossOriginSources() {
        assertThatThrownBy(() -> guard.requireAllowedSource(null, null)).isInstanceOf(IdentityFailure.class);
        assertThatThrownBy(() -> guard.requireAllowedSource(null, "not a uri")).isInstanceOf(IdentityFailure.class);
        assertThatThrownBy(() -> guard.requireAllowedSource("https://attacker.example", null))
                .isInstanceOf(IdentityFailure.class)
                .extracting(failure -> ((IdentityFailure) failure).code())
                .isEqualTo(IdentityFailure.Code.INVALID_CSRF);
    }
}
