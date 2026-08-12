package com.commitquest.preview.application;

import com.commitquest.preview.domain.CampaignProjection;
import com.commitquest.preview.domain.CampaignProjection.Chapter;
import com.commitquest.preview.domain.CampaignProjection.Encounter;
import com.commitquest.preview.domain.CampaignProjection.Metric;
import com.commitquest.preview.domain.CampaignProjection.Position;
import com.commitquest.preview.domain.CampaignProjection.Quest;
import com.commitquest.preview.domain.CampaignProjection.Region;
import com.commitquest.preview.domain.RepositoryEvidence;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public final class CampaignProjectionMapper {

    public CampaignProjection map(RepositoryEvidence source) {
        var repository = source.repository();
        var repositoryUrl = repository.webUrl();
        var title = titleCase(repository.name()) + " Frontier";
        var latestRelease = source.releases().isEmpty() ? source.defaultBranch() : source.releases().getFirst().tag();

        return new CampaignProjection(
                2,
                3,
                1,
                slug(repository.fullName()),
                title,
                repository.fullName(),
                "preview",
                latestRelease,
                metrics(source),
                regions(source, repositoryUrl),
                quests(source),
                encounters(source),
                chapters(source));
    }

    private static List<Metric> metrics(RepositoryEvidence source) {
        return List.of(
                new Metric(Integer.toString(source.pullRequests().size()), "merged encounters sampled"),
                new Metric(Integer.toString(source.releases().size()), "released chapters sampled"),
                new Metric(Integer.toString(source.workflows().size()), "CI defenses discovered"),
                new Metric(Integer.toString(source.issues().size()), "open candidate quests sampled"));
    }

    private static List<Region> regions(RepositoryEvidence source, String repositoryUrl) {
        var rootSummary = source.rootEntries().isEmpty()
                ? "GitHub did not expose any root entries for this repository."
                : "Mapped root entries: " + String.join(", ", source.rootEntries()) + ".";

        return List.of(
                new Region(
                        "repository-gate",
                        "verified",
                        "REGION · REPOSITORY",
                        "Repository Gate",
                        source.description(),
                        "Default branch: " + source.defaultBranch() + " · Primary language: " + source.primaryLanguage(),
                        repositoryUrl,
                        source.archived() ? "Archived" : "Public",
                        source.archived() ? "amber" : "mint",
                        "⌂",
                        new Position(125, 190)),
                new Region(
                        "codebase-province",
                        "inferred",
                        "REGION · CODEBASE",
                        "Codebase Province",
                        rootSummary,
                        "Interpretation of verified root-directory evidence",
                        repositoryUrl,
                        source.rootEntries().isEmpty() ? "Unknown" : "Mapped",
                        source.rootEntries().isEmpty() ? "amber" : "blue",
                        "◆",
                        new Position(365, 135)),
                new Region(
                        "quest-board",
                        "repository-authored",
                        "REGION · OPEN ISSUES",
                        "Quest Board",
                        countDescription(source.issues().size(), "open repository-authored issue", "open repository-authored issues"),
                        "Sample limited to the 10 most recently updated open issues",
                        repositoryUrl + "/issues",
                        source.issues().isEmpty() ? "Quiet" : "Calling",
                        source.issues().isEmpty() ? "amber" : "blue",
                        "?",
                        new Position(625, 185)),
                new Region(
                        "encounter-archive",
                        "verified",
                        "REGION · MERGED PULL REQUESTS",
                        "Encounter Archive",
                        countDescription(source.pullRequests().size(), "merged pull request", "merged pull requests"),
                        "Sample limited to the 10 most recently updated closed pull requests",
                        repositoryUrl + "/pulls?q=is%3Apr+is%3Amerged",
                        source.pullRequests().isEmpty() ? "Uncharted" : "Recorded",
                        source.pullRequests().isEmpty() ? "amber" : "mint",
                        "⚔",
                        new Position(165, 355)),
                new Region(
                        "defense-bastion",
                        "verified",
                        "DEFENSE · GITHUB ACTIONS",
                        "Defense Bastion",
                        countDescription(source.workflows().size(), "repository workflow", "repository workflows"),
                        "Workflow presence is evidence; run success is not inferred",
                        repositoryUrl + "/actions",
                        source.workflows().isEmpty() ? "Not observed" : "Configured",
                        source.workflows().isEmpty() ? "amber" : "mint",
                        "✓",
                        new Position(400, 330)),
                new Region(
                        "chapter-beacon",
                        "verified",
                        "REGION · RELEASES",
                        "Chapter Beacon",
                        countDescription(source.releases().size(), "published release", "published releases"),
                        "Sample limited to the 10 most recent GitHub releases",
                        repositoryUrl + "/releases",
                        source.releases().isEmpty() ? "Unlit" : "Kindled",
                        source.releases().isEmpty() ? "amber" : "blue",
                        "◉",
                        new Position(635, 350)));
    }

    private static List<Quest> quests(RepositoryEvidence source) {
        return source.issues().stream()
                .map(issue -> new Quest(
                        "issue-" + issue.number(),
                        "quest-board",
                        "repository-authored",
                        issue.title(),
                        "Open issue presented as a candidate quest; no completion is inferred.",
                        "candidate",
                        "GitHub issue #" + issue.number(),
                        issue.url()))
                .toList();
    }

    private static List<Encounter> encounters(RepositoryEvidence source) {
        return source.pullRequests().stream()
                .map(pullRequest -> new Encounter(
                        "pull-request-" + pullRequest.number(),
                        pullRequest.title(),
                        "Merged pull request verified by GitHub" + dateSuffix(pullRequest.mergedAt()),
                        "victory",
                        pullRequest.number(),
                        "Repository preview",
                        pullRequest.url()))
                .toList();
    }

    private static List<Chapter> chapters(RepositoryEvidence source) {
        var chapters = new ArrayList<Chapter>();
        IntStream.range(0, source.releases().size()).forEach(index -> {
            var release = source.releases().get(index);
            chapters.add(new Chapter(
                    release.tag(),
                    release.name().isBlank() ? "Release " + release.tag() : release.name(),
                    "Published GitHub release" + dateSuffix(release.publishedAt()),
                    index == 0 ? "current" : "unlocked",
                    release.url()));
        });
        return List.copyOf(chapters);
    }

    private static String countDescription(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural) + " found in the bounded preview sample.";
    }

    private static String dateSuffix(String value) {
        return value == null || value.isBlank() ? "." : " on " + value.substring(0, Math.min(10, value.length())) + ".";
    }

    private static String slug(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private static String titleCase(String value) {
        var words = value.replace('-', ' ').replace('_', ' ').split("\\s+");
        return java.util.Arrays.stream(words)
                .filter(word -> !word.isBlank())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse("Repository");
    }
}
