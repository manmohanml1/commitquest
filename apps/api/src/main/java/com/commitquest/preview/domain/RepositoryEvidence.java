package com.commitquest.preview.domain;

import java.util.List;

public record RepositoryEvidence(
        RepositoryRef repository,
        String description,
        String defaultBranch,
        String primaryLanguage,
        boolean archived,
        List<String> rootEntries,
        List<Issue> issues,
        List<PullRequest> pullRequests,
        List<Release> releases,
        List<Workflow> workflows) {

    public RepositoryEvidence {
        description = description == null || description.isBlank()
                ? "No repository description is available."
                : description;
        primaryLanguage = primaryLanguage == null || primaryLanguage.isBlank()
                ? "Not reported"
                : primaryLanguage;
        rootEntries = List.copyOf(rootEntries);
        issues = List.copyOf(issues);
        pullRequests = List.copyOf(pullRequests);
        releases = List.copyOf(releases);
        workflows = List.copyOf(workflows);
    }

    public record Issue(long number, String title, String url) {}

    public record PullRequest(long number, String title, String url, String mergedAt) {}

    public record Release(String tag, String name, String url, String publishedAt) {}

    public record Workflow(long id, String name, String state, String url) {}
}
