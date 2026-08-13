package com.commitquest.preview.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.commitquest.preview.domain.RepositoryEvidence;
import com.commitquest.preview.domain.RepositoryRef;
import java.util.List;
import org.junit.jupiter.api.Test;

class PublicPortfolioCompatibilityTest {

    private final CampaignProjectionMapper mapper = new CampaignProjectionMapper();

    @Test
    void preservesTheAuditedPublicRepositoryShapes() {
        var shapes = List.of(
                new Shape("commitquest", true, true, "full"),
                new Shape("Leetcode-Practice", false, true, "history"),
                new Shape("portfolio-website", true, true, "full"),
                new Shape("novel-browser-glass", true, true, "full"),
                new Shape("checkmate-glass-mrbd", true, false, "foundation"),
                new Shape("autonomous-travel-guide-mrbd", true, true, "full"),
                new Shape("glass-tube", true, false, "foundation"),
                new Shape("glass-search-meta-display", true, false, "foundation"),
                new Shape("Scalable-Data-Processing-System-for-High-Volume-Workloads", false, true, "history"),
                new Shape("Typescript-Practice", false, true, "history"),
                new Shape("OpenGL_Glut_Game", false, false, "foundation"),
                new Shape("Langchain-Project-1", false, true, "history"),
                new Shape("Movies-API", false, false, "foundation"),
                new Shape("Fitness-exercises-app", false, false, "foundation"),
                new Shape("Research-Papers", false, false, "foundation"),
                new Shape("Software-Engineering-Design-Patterns", false, false, "foundation"),
                new Shape("CSCI-174-Team-4", false, false, "foundation"));

        shapes.forEach(this::assertShape);
    }

    private void assertShape(Shape shape) {
        var repositoryName = shape.repositoryName();
        var hasRoadmap = shape.hasRoadmap();
        var hasMergedPullRequest = shape.hasMergedPullRequest();
        var repositoryUrl = "https://github.com/manmohanml1/" + repositoryName;
        var evidence = new RepositoryEvidence(
                new RepositoryRef("manmohanml1", repositoryName),
                "Compatibility fixture",
                "GitHub repository metadata",
                "main",
                "JavaScript",
                false,
                "2026-08-11T12:00:00Z",
                List.of("README.md"),
                List.of(),
                hasRoadmap
                        ? List.of(new RepositoryEvidence.RoadmapItem(
                                "next-step", "Next step", "Planned", "ROADMAP.md", repositoryUrl + "/blob/main/ROADMAP.md"))
                        : List.of(),
                hasMergedPullRequest
                        ? List.of(new RepositoryEvidence.PullRequest(
                                1, "Deliver feature", repositoryUrl + "/pull/1", "2026-08-10T12:00:00Z"))
                        : List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new RepositoryEvidence.Commit(
                        "abcdef012345", "Repository foundation", repositoryUrl + "/commit/abcdef012345", "2026-08-09T12:00:00Z")));

        var projection = mapper.map(evidence);

        assertThat(projection.mode()).as(repositoryName).isEqualTo(shape.expectedMode());
        assertThat(projection.chapters()).isNotEmpty();
        assertThat(projection.encounters()).isNotEmpty();
        assertThat(projection.quests()).hasSize(1);
        if (!hasRoadmap) {
            assertThat(projection.quests().getFirst().level()).isEqualTo("inferred");
            assertThat(projection.quests().getFirst().status()).isEqualTo("recommended");
        }
    }

    private record Shape(
            String repositoryName, boolean hasRoadmap, boolean hasMergedPullRequest, String expectedMode) {}
}
