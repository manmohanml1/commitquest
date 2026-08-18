package com.commitquest.identity.web;

import com.commitquest.identity.application.IdentityFailure;
import com.commitquest.identity.application.IdentityService;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.preview.web.WebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "commitquest.identity", name = "enabled", havingValue = "true")
public final class BrowserAuthentication {

    public static final String SESSION_COOKIE = "__Host-commitquest_session";
    public static final String CSRF_COOKIE = "commitquest_csrf";
    public static final String CSRF_HEADER = "X-CommitQuest-CSRF";

    private final IdentityService identityService;
    private final BrowserRequestGuard requestGuard;

    public BrowserAuthentication(IdentityService identityService, WebProperties webProperties) {
        this.identityService = identityService;
        this.requestGuard = new BrowserRequestGuard(webProperties);
    }

    public AccountId requireAccount(String sessionToken) {
        return identityService.authenticate(sessionToken).account().id();
    }

    public AccountId requireMutation(
            String sessionToken,
            String csrfCookie,
            String csrfHeader,
            String origin,
            String referer) {
        requestGuard.requireAllowedSource(origin, referer);
        if (csrfCookie == null || csrfHeader == null || !csrfCookie.equals(csrfHeader)) {
            throw new IdentityFailure(
                    IdentityFailure.Code.INVALID_CSRF,
                    "The request verification cookie and header do not match.");
        }
        return identityService.authenticateMutation(sessionToken, csrfHeader).account().id();
    }
}
