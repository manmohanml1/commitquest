package com.commitquest.identity.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.commitquest.identity.application.IdentityFailure;
import com.commitquest.identity.application.IdentityService;
import com.commitquest.identity.domain.Account;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.identity.domain.UserSession;
import com.commitquest.preview.web.WebProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BrowserAuthenticationTest {

    @Test
    void authenticatesReadsAndRequiresOriginPlusDoubleSubmitCsrfForMutations() {
        var service = mock(IdentityService.class);
        var account = new Account(
                new AccountId(new UUID(0, 1)),
                42,
                "octocat",
                "Octocat",
                "https://avatars.example/octocat",
                Instant.EPOCH,
                Instant.EPOCH);
        var authenticated = new IdentityService.AuthenticatedSession(account, mock(UserSession.class));
        when(service.authenticate("session")).thenReturn(authenticated);
        when(service.authenticateMutation("session", "csrf")).thenReturn(authenticated);
        var browser = new BrowserAuthentication(
                service, new WebProperties(List.of("https://commitquest.example")));

        assertThat(browser.requireAccount("session")).isEqualTo(account.id());
        assertThat(browser.requireMutation(
                        "session", "csrf", "csrf", "https://commitquest.example", null))
                .isEqualTo(account.id());
        verify(service).authenticate("session");
        verify(service).authenticateMutation("session", "csrf");

        var untouched = mock(IdentityService.class);
        var guarded = new BrowserAuthentication(
                untouched, new WebProperties(List.of("https://commitquest.example")));
        assertThatThrownBy(() -> guarded.requireMutation(
                        "session", "csrf", "different", "https://attacker.example", null))
                .isInstanceOf(IdentityFailure.class);
        verifyNoInteractions(untouched);
    }
}
