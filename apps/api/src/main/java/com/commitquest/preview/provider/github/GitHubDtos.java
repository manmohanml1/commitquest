package com.commitquest.preview.provider.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

final class GitHubDtos {

    private GitHubDtos() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Repository(
            String description,
            @JsonProperty("default_branch") String defaultBranch,
            String language,
            boolean archived,
            @JsonProperty("pushed_at") String pushedAt,
            @JsonProperty("private") boolean privateRepository) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(String name, String type) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Issue(
            long number,
            String title,
            @JsonProperty("html_url") String htmlUrl,
            @JsonProperty("pull_request") Map<String, Object> pullRequest) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PullRequest(
            long number,
            String title,
            @JsonProperty("html_url") String htmlUrl,
            @JsonProperty("merged_at") String mergedAt) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Release(
            @JsonProperty("tag_name") String tagName,
            String name,
            @JsonProperty("html_url") String htmlUrl,
            @JsonProperty("published_at") String publishedAt) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Workflows(List<Workflow> workflows) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Workflow(long id, String name, String state, @JsonProperty("html_url") String htmlUrl) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentDetail(
            String name,
            String path,
            @JsonProperty("html_url") String htmlUrl,
            String encoding,
            String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Tag(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Commit(
            String sha,
            @JsonProperty("html_url") String htmlUrl,
            CommitDetail commit) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CommitDetail(String message, CommitAuthor author) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CommitAuthor(String date) {}
}
