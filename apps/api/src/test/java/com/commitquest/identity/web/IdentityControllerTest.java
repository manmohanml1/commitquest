package com.commitquest.identity.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commitquest.identity.application.IdentityService;
import com.commitquest.identity.domain.Account;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.identity.provider.github.IdentityProperties;
import com.commitquest.preview.web.WebProperties;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class IdentityControllerTest {

    private IdentityService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(IdentityService.class);
        var controller = new IdentityController(
                service,
                properties(),
                new WebProperties(List.of("https://commitquest.example")));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new IdentityExceptionHandler())
                .build();
    }

    @Test
    void redirectsToGitHubAndIssuesHardenedCookiesOnCallback() throws Exception {
        when(service.begin("/#campaign")).thenReturn(URI.create("https://github.test/authorize?state=opaque"));
        when(service.complete("code", "state"))
                .thenReturn(new IdentityService.LoginResult(
                        account(), "session-token", "csrf-token", "/#campaign"));

        mvc.perform(get("/api/v1/auth/github").param("returnPath", "/#campaign"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://github.test/authorize?state=opaque"));
        var callback = mvc.perform(get("/api/v1/auth/github/callback").param("code", "code").param("state", "state"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://commitquest.example/#campaign"))
                .andReturn();
        assertThat(callback.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("__Host-commitquest_session=session-token", "HttpOnly", "Secure", "SameSite=Lax"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("commitquest_csrf=csrf-token", "Secure", "SameSite=Lax")
                        .doesNotContain("HttpOnly"));
    }

    @Test
    void logoutRequiresAllowedOriginAndMatchingDoubleSubmitToken() throws Exception {
        mvc.perform(post("/api/v1/session/logout")
                        .cookie(new Cookie(IdentityController.SESSION_COOKIE, "session"))
                        .cookie(new Cookie(IdentityController.CSRF_COOKIE, "csrf"))
                        .header(IdentityController.CSRF_HEADER, "csrf")
                        .header(HttpHeaders.ORIGIN, "https://commitquest.example"))
                .andExpect(status().isNoContent());
        verify(service).logout("session", "csrf");

        var untouched = mock(IdentityService.class);
        var guardedMvc = MockMvcBuilders.standaloneSetup(new IdentityController(
                        untouched,
                        properties(),
                        new WebProperties(List.of("https://commitquest.example"))))
                .setControllerAdvice(new IdentityExceptionHandler())
                .build();
        guardedMvc.perform(delete("/api/v1/account")
                        .cookie(new Cookie(IdentityController.SESSION_COOKIE, "session"))
                        .cookie(new Cookie(IdentityController.CSRF_COOKIE, "csrf"))
                        .header(IdentityController.CSRF_HEADER, "different")
                        .header(HttpHeaders.ORIGIN, "https://attacker.example"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(untouched);
    }

    private static Account account() {
        var now = Instant.parse("2026-08-17T12:00:00Z");
        return new Account(
                new AccountId(new UUID(0, 1)),
                42,
                "octocat",
                "The Octocat",
                "https://avatars.example/octocat",
                now,
                now);
    }

    private static IdentityProperties properties() {
        return new IdentityProperties(
                true,
                "client",
                "secret",
                URI.create("https://commitquest.example"),
                URI.create("https://github.test/authorize"),
                URI.create("https://github.test/token"),
                URI.create("https://api.github.test"),
                Base64.getEncoder().encodeToString(new byte[32]),
                Duration.ofSeconds(3),
                Duration.ofSeconds(8));
    }
}
