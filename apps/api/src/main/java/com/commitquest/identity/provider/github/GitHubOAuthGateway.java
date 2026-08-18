package com.commitquest.identity.provider.github;

import com.commitquest.identity.application.IdentityFailure;
import com.commitquest.identity.application.OAuthIdentityGateway;
import com.commitquest.identity.domain.GitHubIdentity;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

public final class GitHubOAuthGateway implements OAuthIdentityGateway {

    private final IdentityProperties properties;
    private final RestClient tokenClient;
    private final RestClient apiClient;

    public GitHubOAuthGateway(IdentityProperties properties, RestClient tokenClient, RestClient apiClient) {
        this.properties = properties;
        this.tokenClient = tokenClient;
        this.apiClient = apiClient;
    }

    @Override
    public URI authorizationUri(String state, String codeChallenge) {
        return UriComponentsBuilder.fromUri(properties.authorizationUrl())
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build(true)
                .toUri();
    }

    @Override
    public GitHubIdentity exchange(String code, String codeVerifier) {
        try {
            var form = new LinkedMultiValueMap<String, String>();
            form.add("client_id", properties.clientId());
            form.add("client_secret", properties.clientSecret());
            form.add("code", code);
            form.add("redirect_uri", properties.redirectUri().toString());
            form.add("code_verifier", codeVerifier);
            var token = tokenClient.post()
                    .uri(properties.tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
            if (token == null || token.access_token() == null || token.access_token().isBlank()) {
                throw providerFailure("GitHub did not issue an identity token.", null);
            }
            if (token.scope() != null && !token.scope().isBlank()) {
                throw providerFailure("GitHub returned permissions that CommitQuest did not request.", null);
            }
            var user = apiClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.access_token())
                    .retrieve()
                    .body(UserResponse.class);
            if (user == null) throw providerFailure("GitHub did not return an identity profile.", null);
            return new GitHubIdentity(user.id(), user.login(), user.name(), user.avatar_url());
        } catch (RestClientException exception) {
            throw providerFailure("GitHub identity is temporarily unavailable.", exception);
        }
    }

    private static IdentityFailure providerFailure(String message, Throwable cause) {
        return new IdentityFailure(IdentityFailure.Code.PROVIDER_UNAVAILABLE, message, cause);
    }

    private record TokenResponse(String access_token, String token_type, String scope) {}

    private record UserResponse(long id, String login, String name, String avatar_url) {}
}
