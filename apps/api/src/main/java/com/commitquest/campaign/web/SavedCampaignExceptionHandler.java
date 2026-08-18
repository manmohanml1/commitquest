package com.commitquest.campaign.web;

import com.commitquest.campaign.application.SavedCampaignLimitExceeded;
import com.commitquest.campaign.application.SavedCampaignNotFound;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ConditionalOnProperty(prefix = "commitquest.identity", name = "enabled", havingValue = "true")
final class SavedCampaignExceptionHandler {

    @ExceptionHandler(SavedCampaignNotFound.class)
    ProblemDetail notFound(SavedCampaignNotFound failure) {
        return problem(HttpStatus.NOT_FOUND, "SAVED_CAMPAIGN_NOT_FOUND", failure.getMessage());
    }

    @ExceptionHandler(SavedCampaignLimitExceeded.class)
    ProblemDetail limitExceeded(SavedCampaignLimitExceeded failure) {
        return problem(HttpStatus.CONFLICT, "SAVED_CAMPAIGN_LIMIT_EXCEEDED", failure.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:commitquest:problem:" + code.toLowerCase().replace('_', '-')));
        problem.setTitle("Saved campaign request failed");
        problem.setProperty("code", code);
        return problem;
    }
}
