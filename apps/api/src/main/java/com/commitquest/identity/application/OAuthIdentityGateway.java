package com.commitquest.identity.application;

import com.commitquest.identity.domain.GitHubIdentity;
import java.net.URI;

public interface OAuthIdentityGateway {

    URI authorizationUri(String state, String codeChallenge);

    GitHubIdentity exchange(String code, String codeVerifier);
}
