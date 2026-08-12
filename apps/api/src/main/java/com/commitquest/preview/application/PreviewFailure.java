package com.commitquest.preview.application;

public final class PreviewFailure extends RuntimeException {

    private final Code code;

    public PreviewFailure(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code code() {
        return code;
    }

    public enum Code {
        INVALID_REPOSITORY_URL,
        PRIVATE_REPOSITORY,
        NOT_FOUND_OR_PRIVATE,
        RATE_LIMITED,
        PROVIDER_UNAVAILABLE,
        INCOMPLETE_REPOSITORY
    }
}
