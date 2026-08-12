package com.commitquest.preview.web;

import com.commitquest.preview.application.PreviewFailure;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
final class PreviewExceptionHandler {

    @ExceptionHandler(PreviewFailure.class)
    ProblemDetail handlePreviewFailure(PreviewFailure failure) {
        var status = switch (failure.code()) {
            case INVALID_REPOSITORY_URL -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND_OR_PRIVATE -> HttpStatus.NOT_FOUND;
            case PRIVATE_REPOSITORY, INCOMPLETE_REPOSITORY -> HttpStatus.UNPROCESSABLE_CONTENT;
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case PROVIDER_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        return problem(status, failure.code().name(), failure.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException failure) {
        var detail = failure.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("The preview request is invalid.");
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", detail);
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:commitquest:problem:" + code.toLowerCase().replace('_', '-')));
        problem.setTitle(title(code));
        problem.setProperty("code", code);
        return problem;
    }

    private static String title(String code) {
        var words = code.toLowerCase().replace('_', ' ').split(" ");
        return java.util.Arrays.stream(words)
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse("Preview error");
    }
}
