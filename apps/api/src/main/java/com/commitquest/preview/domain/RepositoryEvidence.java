package com.commitquest.preview.domain;

import java.util.List;

public record RepositoryEvidence(
        RepositoryRef repository,
        String description,
        String descriptionSource,
        String defaultBranch,
        String primaryLanguage,
        boolean archived,
        String pushedAt,
        List<String> rootEntries,
        List<Issue> issues,
        List<RoadmapItem> roadmapItems,
        List<PullRequest> pullRequests,
        List<Release> releases,
        List<Tag> tags,
        List<Workflow> workflows,
        List<Commit> commits) {

    public RepositoryEvidence {
        description = description == null || description.isBlank()
                ? "No repository description is available."
                : description;
        descriptionSource = descriptionSource == null || descriptionSource.isBlank()
                ? "Unavailable"
                : descriptionSource;
        primaryLanguage = primaryLanguage == null || primaryLanguage.isBlank()
                ? "Not reported"
                : primaryLanguage;
        rootEntries = List.copyOf(rootEntries);
        issues = List.copyOf(issues);
        roadmapItems = List.copyOf(roadmapItems);
        pullRequests = List.copyOf(pullRequests);
        releases = List.copyOf(releases);
        tags = List.copyOf(tags);
        workflows = List.copyOf(workflows);
        commits = List.copyOf(commits);
    }

    public record Issue(long number, String title, String url) {}

    public record RoadmapItem(String id, String title, String summary, String sourceLabel, String url) {}

    public record PullRequest(long number, String title, String url, String mergedAt) {}

    public record Release(String tag, String name, String url, String publishedAt) {}

    public record Tag(String name, String url) {}

    public record Workflow(long id, String name, String state, String url) {}

    public record Commit(String sha, String title, String url, String committedAt) {}
}
