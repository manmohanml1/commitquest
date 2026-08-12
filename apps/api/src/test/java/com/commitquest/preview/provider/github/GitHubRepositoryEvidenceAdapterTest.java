package com.commitquest.preview.provider.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.commitquest.preview.domain.RepositoryRef;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GitHubRepositoryEvidenceAdapterTest {

    @Test
    void normalizesBoundedGitHubResponsesWithoutLeakingProviderDtos() {
        var builder = RestClient.builder().baseUrl("https://api.github.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        var adapter = new GitHubRepositoryEvidenceAdapter(builder.build());

        server.expect(requestTo("https://api.github.test/repos/owner/repository"))
                .andRespond(withSuccess(
                        """
                        {"description":"Evidence source","default_branch":"main","language":"Java","archived":false,"private":false}
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.github.test/repos/owner/repository/contents"))
                .andRespond(withSuccess(
                        """
                        [{"name":"src","type":"dir"},{"name":"README.md","type":"file"}]
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                        "https://api.github.test/repos/owner/repository/issues?state=open&sort=updated&direction=desc&per_page=10"))
                .andRespond(withSuccess(
                        """
                        [{"number":8,"title":"Open quest","html_url":"https://github.com/owner/repository/issues/8"},
                         {"number":7,"title":"Pull request","html_url":"https://github.com/owner/repository/pull/7","pull_request":{}}]
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                        "https://api.github.test/repos/owner/repository/pulls?state=closed&sort=updated&direction=desc&per_page=10"))
                .andRespond(withSuccess(
                        """
                        [{"number":7,"title":"Merged encounter","html_url":"https://github.com/owner/repository/pull/7","merged_at":"2026-08-10T12:00:00Z"},
                         {"number":6,"title":"Closed only","html_url":"https://github.com/owner/repository/pull/6","merged_at":null}]
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.github.test/repos/owner/repository/releases?per_page=10"))
                .andRespond(withSuccess(
                        """
                        [{"tag_name":"v1.0.0","name":"First release","html_url":"https://github.com/owner/repository/releases/tag/v1.0.0","published_at":"2026-08-11T12:00:00Z"}]
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                        "https://api.github.test/repos/owner/repository/actions/workflows?per_page=10"))
                .andRespond(withSuccess(
                        """
                        {"workflows":[{"id":10,"name":"Quality","state":"active","html_url":"https://github.com/owner/repository/actions/workflows/quality.yml"}]}
                        """,
                        MediaType.APPLICATION_JSON));

        var evidence = adapter.load(new RepositoryRef("owner", "repository"));

        assertThat(evidence.repository().fullName()).isEqualTo("owner/repository");
        assertThat(evidence.rootEntries()).containsExactly("src", "README.md");
        assertThat(evidence.issues()).singleElement().satisfies(issue ->
                assertThat(issue.number()).isEqualTo(8));
        assertThat(evidence.pullRequests()).singleElement().satisfies(pullRequest ->
                assertThat(pullRequest.number()).isEqualTo(7));
        assertThat(evidence.releases()).singleElement().satisfies(release ->
                assertThat(release.tag()).isEqualTo("v1.0.0"));
        assertThat(evidence.workflows()).singleElement().satisfies(workflow ->
                assertThat(workflow.name()).isEqualTo("Quality"));
        server.verify();
    }
}
