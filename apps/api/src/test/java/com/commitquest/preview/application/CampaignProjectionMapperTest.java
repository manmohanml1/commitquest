package com.commitquest.preview.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.commitquest.preview.domain.CampaignProjection;
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
                "2026-08-11T12:00:00Z",
                List.of("src", "README.md"),
                List.of(new RepositoryEvidence.Issue(7, "Document the API", "https://github.com/owner/sample-repo/issues/7")),
                List.of(new RepositoryEvidence.RoadmapItem(
                        "add-export",
                        "Add export",
                        "Planned in the roadmap.",
                        "ROADMAP.md",
                        "https://github.com/owner/sample-repo/blob/main/ROADMAP.md")),
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
                List.of(new RepositoryEvidence.Tag("v1.0.0", "https://github.com/owner/sample-repo/releases/tag/v1.0.0")),
                List.of(new RepositoryEvidence.Workflow(
                        10,
                        "Quality",
                        "active",
                        "https://github.com/owner/sample-repo/actions/workflows/quality.yml")),
                List.of(new RepositoryEvidence.Commit(
                        "abcdef012345",
                        "Initial foundation",
                        "https://github.com/owner/sample-repo/commit/abcdef012345",
                        "2026-08-09T12:00:00Z")));

        var projection = mapper.map(source);

        assertThat(projection.schemaVersion()).isEqualTo(3);
        assertThat(projection.mappingAlgorithmVersion()).isEqualTo(4);
        assertThat(projection.scoringRulesetVersion()).isEqualTo(1);
        assertThat(projection.mode()).isEqualTo("full");
        assertThat(projection.repository()).isEqualTo("owner/sample-repo");
        assertThat(projection.title()).isEqualTo("Sample Repo Frontier");
        assertThat(projection.evidence()).hasSize(6);
        assertThat(projection.quests()).hasSize(2);
        assertThat(projection.quests()).extracting(CampaignProjection.Quest::level)
                .containsExactly("verified", "repository-authored");
        assertThat(projection.encounters()).singleElement().satisfies(encounter ->
                assertThat(encounter.reference()).isEqualTo("PR #6"));
        assertThat(projection.chapters()).singleElement().satisfies(chapter ->
                assertThat(chapter.status()).isEqualTo("current"));
    }

    @Test
    void classifiesFoundationAndUsesClearlyLabelledFallbackEvidence() {
        var source = new RepositoryEvidence(
                new RepositoryRef("owner", "small-repo"),
                "Small project",
                "main",
                "JavaScript",
                false,
                "2026-08-11T12:00:00Z",
                List.of("README.md", "index.js"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new RepositoryEvidence.Commit(
                        "abcdef012345",
                        "Build the prototype",
                        "https://github.com/owner/small-repo/commit/abcdef012345",
                        "2026-08-11T12:00:00Z")));

        var projection = mapper.map(source);

        assertThat(projection.mode()).isEqualTo("foundation");
        assertThat(projection.quests()).isEmpty();
        assertThat(projection.encounters()).singleElement().satisfies(encounter -> {
            assertThat(encounter.level()).isEqualTo("inferred");
            assertThat(encounter.status()).isEqualTo("observed");
        });
        assertThat(projection.chapters()).singleElement().satisfies(chapter -> {
            assertThat(chapter.level()).isEqualTo("inferred");
            assertThat(chapter.kind()).isEqualTo("foundation");
        });
    }

    @Test
    void distinguishesHistoryAndArchiveCampaigns() {
        var history = emptyEvidence(false, List.of(new RepositoryEvidence.PullRequest(
                4, "Ship it", "https://github.com/owner/sample/pull/4", "2026-08-10T12:00:00Z")));
        var archive = emptyEvidence(true, List.of());

        assertThat(mapper.map(history).mode()).isEqualTo("history");
        assertThat(mapper.map(archive).mode()).isEqualTo("archive");
    }

    private static RepositoryEvidence emptyEvidence(
            boolean archived, List<RepositoryEvidence.PullRequest> pullRequests) {
        return new RepositoryEvidence(
                new RepositoryRef("owner", "sample"),
                "Sample",
                "main",
                "Java",
                archived,
                "2026-08-11T12:00:00Z",
                List.of("README.md"),
                List.of(),
                List.of(),
                pullRequests,
                List.of(),
                List.of(),
                List.of(),
                List.of(new RepositoryEvidence.Commit(
                        "abcdef012345", "Initial", "https://github.com/owner/sample/commit/abcdef012345", "2026-08-11T12:00:00Z")));
    }
}
