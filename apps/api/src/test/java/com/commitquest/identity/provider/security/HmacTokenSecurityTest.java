package com.commitquest.identity.provider.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class HmacTokenSecurityTest {

    @Test
    void createsOpaqueTokensAndSeparatedStableDigests() {
        var security = new HmacTokenSecurity(Base64.getDecoder().decode(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="));

        assertThat(security.randomToken()).hasSize(43).doesNotContain("=");
        assertThat(security.digest("state")).hasSize(64).isNotEqualTo(security.digest("other"));
        assertThat(security.codeVerifier("state")).hasSize(43);
        assertThat(security.codeChallenge(security.codeVerifier("state"))).hasSize(43);
    }
}
