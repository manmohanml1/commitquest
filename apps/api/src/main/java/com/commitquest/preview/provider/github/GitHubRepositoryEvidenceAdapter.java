package com.commitquest.preview.provider.github;

import com.commitquest.preview.application.PreviewFailure;
import com.commitquest.preview.application.RepositoryEvidencePort;
import com.commitquest.preview.domain.RepositoryEvidence;
import com.commitquest.preview.domain.RepositoryRef;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
final class GitHubRepositoryEvidenceAdapter implements RepositoryEvidencePort {

    private static final int SAMPLE_LIMIT = 10;
    private final RestClient client;

    GitHubRepositoryEvidenceAdapter(RestClient gitHubRestClient) {
        this.client = gitHubRestClient;
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

            var rootEntries = getList(path + "/contents", GitHubDtos.Content[].class, repository).stream()
                    .map(GitHubDtos.Content::name)
                    .limit(8)
                    .toList();
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
                            path + "/pulls?state=closed&sort=updated&direction=desc&per_page=" + SAMPLE_LIMIT,
                            GitHubDtos.PullRequest[].class,
                            repository)
                    .stream()
                    .filter(pullRequest -> pullRequest.mergedAt() != null)
                    .map(pullRequest -> new RepositoryEvidence.PullRequest(
                            pullRequest.number(),
                            pullRequest.title(),
                            pullRequest.htmlUrl(),
                            pullRequest.mergedAt()))
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

            return new RepositoryEvidence(
                    repository,
                    metadata.description(),
                    metadata.defaultBranch(),
                    metadata.language(),
                    metadata.archived(),
                    rootEntries,
                    issues,
                    pullRequests,
                    releases,
                    workflows);
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
