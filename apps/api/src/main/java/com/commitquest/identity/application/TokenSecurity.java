package com.commitquest.identity.application;

public interface TokenSecurity {

    String randomToken();

    String digest(String token);

    String codeVerifier(String state);

    String codeChallenge(String codeVerifier);
}
