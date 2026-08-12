package com.commitquest.preview.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.commitquest.preview.domain.RepositoryEvidence;
import com.commitquest.preview.domain.RepositoryRef;
import java.util.List;
import org.junit.jupiter.api.Test;

class CampaignProjectionMapperTest {

    private final CampaignProjectionMapper mapper = new CampaignProjectionMapper();

    @Test
    void mapsProviderNeutralEvidenceDeterministically() {
        var source = new RepositoryEvidence(
                new RepositoryRef("owner", "sample-repo"),
                "A sample repository",
                "main",
                "Java",
                false,
                List.of("src", "README.md"),
                List.of(new RepositoryEvidence.Issue(7, "Document the API", "https://github.com/owner/sample-repo/issues/7")),
                List.of(new RepositoryEvidence.PullRequest(
                        6,
                        "Add the API",
                        "https://github.com/owner/sample-repo/pull/6",
                        "2026-08-10T12:00:00Z")),
                List.of(new RepositoryEvidence.Release(
                        "v1.0.0",
                        "First release",
                        "https://github.com/owner/sample-repo/releases/tag/v1.0.0",
                        "2026-08-11T12:00:00Z")),
                List.of(new RepositoryEvidence.Workflow(
                        10,
                        "Quality",
                        "active",
                        "https://github.com/owner/sample-repo/actions/workflows/quality.yml")));

        var projection = mapper.map(source);

        assertThat(projection.schemaVersion()).isEqualTo(2);
        assertThat(projection.mappingAlgorithmVersion()).isEqualTo(3);
        assertThat(projection.scoringRulesetVersion()).isEqualTo(1);
        assertThat(projection.mode()).isEqualTo("preview");
        assertThat(projection.repository()).isEqualTo("owner/sample-repo");
        assertThat(projection.title()).isEqualTo("Sample Repo Frontier");
        assertThat(projection.evidence()).hasSize(6);
        assertThat(projection.quests()).singleElement().satisfies(quest -> {
            assertThat(quest.level()).isEqualTo("repository-authored");
            assertThat(quest.status()).isEqualTo("candidate");
        });
        assertThat(projection.encounters()).singleElement().satisfies(encounter ->
                assertThat(encounter.pullRequest()).isEqualTo(6));
        assertThat(projection.chapters()).singleElement().satisfies(chapter ->
                assertThat(chapter.status()).isEqualTo("current"));
    }
}
