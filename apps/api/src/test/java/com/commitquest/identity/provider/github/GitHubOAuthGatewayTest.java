package com.commitquest.identity.provider.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.commitquest.identity.application.IdentityFailure;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GitHubOAuthGatewayTest {

    @Test
    void requestsNoScopeUsesPkceAndDiscardsTheTokenAfterIdentityLookup() {
        var tokenBuilder = RestClient.builder();
        var apiBuilder = RestClient.builder().baseUrl("https://api.github.test");
        var tokenServer = MockRestServiceServer.bindTo(tokenBuilder).build();
        var apiServer = MockRestServiceServer.bindTo(apiBuilder).build();
        var gateway = new GitHubOAuthGateway(properties(), tokenBuilder.build(), apiBuilder.build());

        var authorization = gateway.authorizationUri("state-value", "challenge-value", false);
        assertThat(authorization.toString())
                .contains("client_id=client-id", "state=state-value", "code_challenge=challenge-value")
                .contains("code_challenge_method=S256")
                .doesNotContain("scope=", "prompt=");

        tokenServer.expect(requestTo("https://github.test/login/oauth/access_token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("code=temporary-code"),
                        org.hamcrest.Matchers.containsString("code_verifier=verifier-value"),
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("scope=")))))
                .andRespond(withSuccess(
                        "{\"access_token\":\"temporary-token\",\"token_type\":\"bearer\",\"scope\":\"\"}",
                        MediaType.APPLICATION_JSON));
        apiServer.expect(requestTo("https://api.github.test/user"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer temporary-token"))
                .andRespond(withSuccess(
                        "{\"id\":42,\"login\":\"octocat\",\"name\":\"The Octocat\","
                                + "\"avatar_url\":\"https://avatars.example/octocat\"}",
                        MediaType.APPLICATION_JSON));

        var identity = gateway.exchange("temporary-code", "verifier-value");

        assertThat(identity.userId()).isEqualTo(42);
        assertThat(identity.login()).isEqualTo("octocat");
        tokenServer.verify();
        apiServer.verify();
    }

    @Test
    void asksGitHubToShowItsAccountPickerWhenRequested() {
        var gateway = new GitHubOAuthGateway(
                properties(), RestClient.builder().build(), RestClient.builder().build());

        assertThat(gateway.authorizationUri("state-value", "challenge-value", true).toString())
                .contains("prompt=select_account")
                .doesNotContain("scope=");
    }

    @Test
    void rejectsUnexpectedOAuthPermissionsBeforeCallingTheUserApi() {
        var tokenBuilder = RestClient.builder();
        var apiBuilder = RestClient.builder().baseUrl("https://api.github.test");
        var tokenServer = MockRestServiceServer.bindTo(tokenBuilder).build();
        var gateway = new GitHubOAuthGateway(properties(), tokenBuilder.build(), apiBuilder.build());
        tokenServer.expect(requestTo("https://github.test/login/oauth/access_token"))
                .andRespond(withSuccess(
                        "{\"access_token\":\"token\",\"token_type\":\"bearer\",\"scope\":\"repo\"}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> gateway.exchange("code", "verifier"))
                .isInstanceOf(IdentityFailure.class)
                .extracting(failure -> ((IdentityFailure) failure).code())
                .isEqualTo(IdentityFailure.Code.PROVIDER_UNAVAILABLE);
        tokenServer.verify();
    }

    private static IdentityProperties properties() {
        return new IdentityProperties(
                true,
                "client-id",
                "client-secret",
                URI.create("https://commitquest.example"),
                URI.create("https://github.test/login/oauth/authorize"),
                URI.create("https://github.test/login/oauth/access_token"),
                URI.create("https://api.github.test"),
                Base64.getEncoder().encodeToString(new byte[32]),
                Duration.ofSeconds(3),
                Duration.ofSeconds(8));
    }
}
