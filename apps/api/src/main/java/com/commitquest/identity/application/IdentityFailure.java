package com.commitquest.identity.application;

public final class IdentityFailure extends RuntimeException {

    public enum Code {
        INVALID_RETURN_PATH,
        INVALID_OAUTH_STATE,
        PROVIDER_UNAVAILABLE,
        UNAUTHENTICATED,
        INVALID_CSRF
    }

    private final Code code;

    public IdentityFailure(Code code, String message) {
        super(message);
        this.code = code;
    }

    public IdentityFailure(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code code() {
        return code;
    }
}
