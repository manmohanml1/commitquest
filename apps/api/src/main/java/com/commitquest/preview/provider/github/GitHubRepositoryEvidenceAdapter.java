package com.commitquest.preview.provider.github;

import com.commitquest.preview.application.PreviewFailure;
import com.commitquest.preview.application.RepositoryEvidencePort;
import com.commitquest.preview.domain.RepositoryEvidence;
import com.commitquest.preview.domain.RepositoryRef;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
final class GitHubRepositoryEvidenceAdapter implements RepositoryEvidencePort {

    private static final int SAMPLE_LIMIT = 10;
    private static final int MAX_ENCODED_DOCUMENT_LENGTH = 180_000;
    private final RestClient client;
    private final RoadmapParser roadmapParser;
    private final ReadmeSummaryParser readmeSummaryParser;

    GitHubRepositoryEvidenceAdapter(
            RestClient gitHubRestClient,
            RoadmapParser roadmapParser,
            ReadmeSummaryParser readmeSummaryParser) {
        this.client = gitHubRestClient;
        this.roadmapParser = roadmapParser;
        this.readmeSummaryParser = readmeSummaryParser;
    }

    @Override
    public RepositoryEvidence load(RepositoryRef repository) {
        try {
            var path = "/repos/{owner}/{repository}";
            var metadata = get(path, GitHubDtos.Repository.class, repository);
            if (metadata.privateRepository()) {
                throw new PreviewFailure(
                        PreviewFailure.Code.PRIVATE_REPOSITORY,
                        "CommitQuest v0.3 can preview only public GitHub repositories.");
            }

            var allRootEntries = getList(path + "/contents", GitHubDtos.Content[].class, repository).stream()
                    .map(GitHubDtos.Content::name)
                    .toList();
            var rootEntries = allRootEntries.stream()
                    .limit(8)
                    .toList();
            var description = loadDescription(path, repository, metadata, allRootEntries);
            var roadmapItems = loadRoadmap(path, repository, allRootEntries);
            var issues = getList(
                            path + "/issues?state=open&sort=updated&direction=desc&per_page=" + SAMPLE_LIMIT,
                            GitHubDtos.Issue[].class,
                            repository)
                    .stream()
                    .filter(issue -> issue.pullRequest() == null)
                    .map(issue -> new RepositoryEvidence.Issue(issue.number(), issue.title(), issue.htmlUrl()))
                    .limit(SAMPLE_LIMIT)
                    .toList();
            var pullRequests = getList(
                            path + "/pulls?state=closed&sort=updated&direction=desc&per_page=30",
                            GitHubDtos.PullRequest[].class,
                            repository)
                    .stream()
                    .filter(pullRequest -> pullRequest.mergedAt() != null)
                    .map(pullRequest -> new RepositoryEvidence.PullRequest(
                            pullRequest.number(),
                            pullRequest.title(),
                            pullRequest.htmlUrl(),
                            pullRequest.mergedAt()))
                    .limit(SAMPLE_LIMIT)
                    .toList();
            var releases = getList(
                            path + "/releases?per_page=" + SAMPLE_LIMIT,
                            GitHubDtos.Release[].class,
                            repository)
                    .stream()
                    .map(release -> new RepositoryEvidence.Release(
                            release.tagName(),
                            release.name() == null ? "" : release.name(),
                            release.htmlUrl(),
                            release.publishedAt()))
                    .toList();
            var tags = releases.isEmpty()
                    ? getList(path + "/tags?per_page=" + SAMPLE_LIMIT, GitHubDtos.Tag[].class, repository).stream()
                            .map(tag -> new RepositoryEvidence.Tag(
                                    tag.name(), repository.webUrl() + "/tree/" + tag.name()))
                            .toList()
                    : List.<RepositoryEvidence.Tag>of();
            var workflowResponse = get(
                    path + "/actions/workflows?per_page=" + SAMPLE_LIMIT,
                    GitHubDtos.Workflows.class,
                    repository);
            var workflows = workflowResponse.workflows() == null
                    ? List.<RepositoryEvidence.Workflow>of()
                    : workflowResponse.workflows().stream()
                            .map(workflow -> new RepositoryEvidence.Workflow(
                                    workflow.id(), workflow.name(), workflow.state(), workflow.htmlUrl()))
                            .toList();
            var commits = pullRequests.isEmpty()
                    ? getList(
                                    path + "/commits?sha=" + metadata.defaultBranch() + "&per_page=" + SAMPLE_LIMIT,
                                    GitHubDtos.Commit[].class,
                                    repository)
                            .stream()
                            .map(commit -> new RepositoryEvidence.Commit(
                                    commit.sha(),
                                    firstLine(commit.commit().message()),
                                    commit.htmlUrl(),
                                    commit.commit().author() == null ? null : commit.commit().author().date()))
                            .toList()
                    : List.<RepositoryEvidence.Commit>of();

            return new RepositoryEvidence(
                    repository,
                    description.text(),
                    description.source(),
                    metadata.defaultBranch(),
                    metadata.language(),
                    metadata.archived(),
                    metadata.pushedAt(),
                    rootEntries,
                    issues,
                    roadmapItems,
                    pullRequests,
                    releases,
                    tags,
                    workflows,
                    commits);
        } catch (PreviewFailure failure) {
            throw failure;
        } catch (RestClientResponseException failure) {
            throw translate(failure);
        } catch (ResourceAccessException failure) {
            throw new PreviewFailure(
                    PreviewFailure.Code.PROVIDER_UNAVAILABLE,
                    "GitHub did not respond within the preview time limit. Try again later.");
        }
    }

    private <T> T get(String path, Class<T> type, RepositoryRef repository) {
        var response = client.get()
                .uri(path, repository.owner(), repository.name())
                .retrieve()
                .body(type);
        if (response == null) {
            throw new PreviewFailure(
                    PreviewFailure.Code.INCOMPLETE_REPOSITORY,
                    "GitHub returned an incomplete response for this repository.");
        }
        return response;
    }

    private <T> List<T> getList(String path, Class<T[]> type, RepositoryRef repository) {
        return Arrays.asList(get(path, type, repository));
    }

    private List<RepositoryEvidence.RoadmapItem> loadRoadmap(
            String path, RepositoryRef repository, List<String> rootEntries) {
        var roadmapName = rootEntries.stream()
                .filter(name -> name.equalsIgnoreCase("ROADMAP.md"))
                .findFirst();
        if (roadmapName.isEmpty()) return List.of();
        var detail = get(path + "/contents/" + roadmapName.get(), GitHubDtos.ContentDetail.class, repository);
        if (!"base64".equalsIgnoreCase(detail.encoding())
                || detail.content() == null
                || detail.content().length() > MAX_ENCODED_DOCUMENT_LENGTH) return List.of();
        var markdown = decode(detail.content());
        return roadmapParser.parse(markdown, detail.htmlUrl());
    }

    private Description loadDescription(
            String path,
            RepositoryRef repository,
            GitHubDtos.Repository metadata,
            List<String> rootEntries) {
        if (metadata.description() != null && !metadata.description().isBlank()) {
            return new Description(metadata.description().trim(), "GitHub repository metadata");
        }

        var readmeName = rootEntries.stream()
                .filter(name -> name.equalsIgnoreCase("README.md") || name.equalsIgnoreCase("README"))
                .findFirst();
        if (readmeName.isPresent()) {
            try {
                var detail = get(path + "/contents/" + readmeName.get(), GitHubDtos.ContentDetail.class, repository);
                if ("base64".equalsIgnoreCase(detail.encoding())
                        && detail.content() != null
                        && detail.content().length() <= MAX_ENCODED_DOCUMENT_LENGTH) {
                    var summary = readmeSummaryParser.parse(decode(detail.content()));
                    if (!summary.isBlank()) return new Description(summary, detail.name() + " introduction");
                }
            } catch (IllegalArgumentException ignored) {
                // A malformed or racing README must not prevent a verified-facts fallback.
            } catch (RestClientResponseException failure) {
                if (failure.getStatusCode() != HttpStatus.NOT_FOUND) throw failure;
            }
        }

        var language = metadata.language() == null || metadata.language().isBlank()
                ? "public"
                : metadata.language();
        return new Description(
                titleCase(repository.name()) + " is a " + language + " repository using "
                        + metadata.defaultBranch() + " as its default branch.",
                "Verified repository facts");
    }

    private static String decode(String content) {
        return new String(Base64.getMimeDecoder().decode(content), StandardCharsets.UTF_8);
    }

    private static String firstLine(String value) {
        return value == null ? "Repository commit" : value.lines().findFirst().orElse("Repository commit");
    }

    private static String titleCase(String value) {
        return Arrays.stream(value.replace('-', ' ').replace('_', ' ').split("\\s+"))
                .filter(word -> !word.isBlank())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse("Repository");
    }

    private record Description(String text, String source) {}

    private static PreviewFailure translate(RestClientResponseException failure) {
        if (failure.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new PreviewFailure(
                    PreviewFailure.Code.NOT_FOUND_OR_PRIVATE,
                    "GitHub could not expose that repository. It may not exist or may be private.");
        }
        var remaining = failure.getResponseHeaders() == null
                ? null
                : failure.getResponseHeaders().getFirst("X-RateLimit-Remaining");
        if (failure.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
                || (failure.getStatusCode() == HttpStatus.FORBIDDEN && "0".equals(remaining))) {
            return new PreviewFailure(
                    PreviewFailure.Code.RATE_LIMITED,
                    "GitHub's public API rate limit is temporarily exhausted. Try again later.");
        }
        return new PreviewFailure(
                PreviewFailure.Code.PROVIDER_UNAVAILABLE,
                "GitHub could not provide the repository preview right now.");
    }
}
