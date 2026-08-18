package com.commitquest.identity.web;

import com.commitquest.identity.application.IdentityFailure;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ConditionalOnProperty(prefix = "commitquest.identity", name = "enabled", havingValue = "true")
final class IdentityExceptionHandler {

    @ExceptionHandler(IdentityFailure.class)
    ProblemDetail handle(IdentityFailure failure) {
        var status = switch (failure.code()) {
            case INVALID_RETURN_PATH, INVALID_OAUTH_STATE -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case INVALID_CSRF -> HttpStatus.FORBIDDEN;
            case PROVIDER_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        var problem = ProblemDetail.forStatusAndDetail(status, failure.getMessage());
        problem.setType(URI.create("urn:commitquest:problem:" + failure.code().name().toLowerCase().replace('_', '-')));
        problem.setTitle("Identity request failed");
        problem.setProperty("code", failure.code().name());
        return problem;
    }
}
