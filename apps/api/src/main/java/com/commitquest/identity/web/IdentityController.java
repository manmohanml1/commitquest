package com.commitquest.identity.web;

import com.commitquest.identity.application.IdentityService;
import com.commitquest.identity.provider.github.IdentityProperties;
import com.commitquest.preview.web.WebProperties;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(prefix = "commitquest.identity", name = "enabled", havingValue = "true")
final class IdentityController {

    static final String SESSION_COOKIE = "__Host-commitquest_session";
    static final String CSRF_COOKIE = "commitquest_csrf";
    static final String CSRF_HEADER = "X-CommitQuest-CSRF";

    private final IdentityService identityService;
    private final IdentityProperties properties;
    private final BrowserRequestGuard requestGuard;

    IdentityController(IdentityService identityService, IdentityProperties properties, WebProperties webProperties) {
        this.identityService = identityService;
        this.properties = properties;
        this.requestGuard = new BrowserRequestGuard(webProperties);
    }

    @GetMapping("/api/v1/auth/github")
    ResponseEntity<Void> begin(
            @RequestParam(defaultValue = "/") String returnPath,
            @RequestParam(defaultValue = "false") boolean selectAccount) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(identityService.begin(returnPath, selectAccount))
                .build();
    }

    @GetMapping("/api/v1/auth/github/callback")
    ResponseEntity<Void> callback(@RequestParam String code, @RequestParam String state) {
        var result = identityService.complete(code, state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(properties.publicBaseUrl().resolve(result.returnPath()))
                .header(HttpHeaders.SET_COOKIE, sessionCookie(result.sessionToken(), IdentityService.SESSION_LIFETIME).toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie(result.csrfToken(), IdentityService.SESSION_LIFETIME).toString())
                .build();
    }

    @GetMapping("/api/v1/session")
    SessionView session(
            @CookieValue(name = SESSION_COOKIE, required = false) String sessionToken,
            @CookieValue(name = CSRF_COOKIE, required = false) String csrfToken) {
        var authenticated = identityService.authenticateMutation(sessionToken, csrfToken);
        var account = authenticated.account();
        return new SessionView(
                account.githubLogin(),
                account.displayName(),
                account.avatarUrl(),
                authenticated.session().expiresAt(),
                csrfToken);
    }

    @PostMapping("/api/v1/session/logout")
    ResponseEntity<Void> logout(
            @CookieValue(name = SESSION_COOKIE, required = false) String sessionToken,
            @CookieValue(name = CSRF_COOKIE, required = false) String csrfCookie,
            @RequestHeader(name = CSRF_HEADER, required = false) String csrfHeader,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
            @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer) {
        requestGuard.requireAllowedSource(origin, referer);
        requireMatchingCsrfCookie(csrfCookie, csrfHeader);
        identityService.logout(sessionToken, csrfHeader);
        return clearedCookies();
    }

    @DeleteMapping("/api/v1/account")
    ResponseEntity<Void> deleteAccount(
            @CookieValue(name = SESSION_COOKIE, required = false) String sessionToken,
            @CookieValue(name = CSRF_COOKIE, required = false) String csrfCookie,
            @RequestHeader(name = CSRF_HEADER, required = false) String csrfHeader,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
            @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer) {
        requestGuard.requireAllowedSource(origin, referer);
        requireMatchingCsrfCookie(csrfCookie, csrfHeader);
        identityService.deleteAccount(sessionToken, csrfHeader);
        return clearedCookies();
    }

    private static void requireMatchingCsrfCookie(String cookie, String header) {
        if (cookie == null || header == null || !cookie.equals(header)) {
            throw new com.commitquest.identity.application.IdentityFailure(
                    com.commitquest.identity.application.IdentityFailure.Code.INVALID_CSRF,
                    "The request verification cookie and header do not match.");
        }
    }

    private static ResponseEntity<Void> clearedCookies() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie("", Duration.ZERO).toString())
                .build();
    }

    private static ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from(SESSION_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private static ResponseCookie csrfCookie(String value, Duration maxAge) {
        return ResponseCookie.from(CSRF_COOKIE, value)
                .httpOnly(false)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    record SessionView(
            String githubLogin,
            String displayName,
            String avatarUrl,
            java.time.Instant expiresAt,
            String csrfToken) {}
}
