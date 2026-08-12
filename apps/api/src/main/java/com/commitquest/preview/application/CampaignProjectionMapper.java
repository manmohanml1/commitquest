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
        var chapters = chapters(source);
        var currentChapter = chapters.isEmpty() ? source.defaultBranch() : chapters.getFirst().version();

        return new CampaignProjection(
                3,
                4,
                1,
                slug(repository.fullName()),
                title,
                repository.fullName(),
                campaignMode(source),
                currentChapter,
                metrics(source),
                regions(source, repositoryUrl),
                quests(source),
                encounters(source),
                chapters);
    }

    private static String campaignMode(RepositoryEvidence source) {
        if (source.archived()) return "archive";
        var hasCandidates = !source.issues().isEmpty() || !source.roadmapItems().isEmpty();
        var hasHistory = !source.pullRequests().isEmpty() || !source.releases().isEmpty() || !source.tags().isEmpty();
        if (hasCandidates && hasHistory) return "full";
        if (hasHistory) return "history";
        return "foundation";
    }

    private static List<Metric> metrics(RepositoryEvidence source) {
        var candidates = source.issues().size() + source.roadmapItems().size();
        var milestones = !source.releases().isEmpty() ? source.releases().size() : source.tags().size();
        return List.of(
                new Metric(sampleValue(source.pullRequests().size()), "merged PR encounters"),
                new Metric(sampleValue(milestones), "release or tag chapters"),
                new Metric(sampleValue(source.workflows().size()), "CI workflows discovered"),
                new Metric(sampleValue(candidates), "issue + roadmap candidates"));
    }

    private static String sampleValue(int value) {
        return value == 10 ? "10+" : Integer.toString(value);
    }

    private static List<Region> regions(RepositoryEvidence source, String repositoryUrl) {
        var rootSummary = source.rootEntries().isEmpty()
                ? "GitHub did not expose any root entries for this repository."
                : "Mapped root entries: " + String.join(", ", source.rootEntries()) + ".";
        var candidates = source.issues().size() + source.roadmapItems().size();
        var codebaseTitle = source.primaryLanguage().equals("Not reported")
                ? "Codebase Province"
                : source.primaryLanguage() + " Province";
        var roadmapUrl = source.roadmapItems().isEmpty()
                ? repositoryUrl + "/issues"
                : source.roadmapItems().getFirst().url();

        return List.of(
                new Region(
                        "repository-gate",
                        "verified",
                        "REGION · " + campaignMode(source).toUpperCase(Locale.ROOT) + " CAMPAIGN",
                        source.archived() ? "Archive Gate" : "Repository Gate",
                        source.description(),
                        "Default branch: " + source.defaultBranch() + " · Primary language: " + source.primaryLanguage(),
                        repositoryUrl,
                        source.archived() ? "Archived" : titleCase(campaignMode(source)),
                        source.archived() ? "amber" : "mint",
                        "⌂",
                        new Position(125, 190)),
                new Region(
                        "codebase-province",
                        "inferred",
                        "REGION · CODEBASE STRUCTURE",
                        codebaseTitle,
                        rootSummary,
                        "Interpretation of verified root-directory evidence",
                        repositoryUrl,
                        source.rootEntries().isEmpty() ? "Unknown" : "Mapped",
                        source.rootEntries().isEmpty() ? "amber" : "blue",
                        "◆",
                        new Position(365, 135)),
                new Region(
                        "quest-board",
                        source.issues().isEmpty() ? "repository-authored" : "verified",
                        "REGION · ISSUES + ROADMAP",
                        "Quest Board",
                        countDescription(candidates, "candidate quest", "candidate quests"),
                        "Up to 10 open issues plus 10 repository-authored roadmap candidates",
                        roadmapUrl,
                        candidates == 0 ? "Quiet" : "Calling",
                        candidates == 0 ? "amber" : "blue",
                        "?",
                        new Position(625, 185)),
                new Region(
                        "encounter-archive",
                        source.pullRequests().isEmpty() ? "inferred" : "verified",
                        "REGION · DELIVERY HISTORY",
                        source.pullRequests().isEmpty() ? "Commit Trail" : "Encounter Archive",
                        source.pullRequests().isEmpty()
                                ? countDescription(source.commits().size(), "recent commit expedition", "recent commit expeditions")
                                : countDescription(source.pullRequests().size(), "merged pull request", "merged pull requests"),
                        source.pullRequests().isEmpty()
                                ? "Commit expeditions are inferred history, never verified victories"
                                : "Up to 10 recently merged pull requests",
                        source.pullRequests().isEmpty() ? repositoryUrl + "/commits" : repositoryUrl + "/pulls?q=is%3Apr+is%3Amerged",
                        source.pullRequests().isEmpty() ? "Observed" : "Recorded",
                        source.pullRequests().isEmpty() ? "amber" : "mint",
                        "⚔",
                        new Position(165, 355)),
                new Region(
                        "defense-bastion",
                        "verified",
                        "DEFENSE · GITHUB ACTIONS",
                        source.workflows().isEmpty() ? "Unconfigured Outpost" : "Defense Bastion",
                        countDescription(source.workflows().size(), "repository workflow", "repository workflows"),
                        "Workflow presence is evidence; run success is not inferred",
                        repositoryUrl + "/actions",
                        source.workflows().isEmpty() ? "Not observed" : "Configured",
                        source.workflows().isEmpty() ? "amber" : "mint",
                        "✓",
                        new Position(400, 330)),
                new Region(
                        "chapter-beacon",
                        source.releases().isEmpty() ? "repository-authored" : "verified",
                        "REGION · MILESTONES",
                        source.releases().isEmpty() ? "Foundation Beacon" : "Chapter Beacon",
                        milestoneDescription(source),
                        source.releases().isEmpty()
                                ? "Tags and the default branch are not presented as GitHub Releases"
                                : "Up to 10 recent GitHub Releases",
                        source.releases().isEmpty() ? repositoryUrl + "/tags" : repositoryUrl + "/releases",
                        source.releases().isEmpty() ? "Grounded" : "Kindled",
                        source.releases().isEmpty() ? "amber" : "blue",
                        "◉",
                        new Position(635, 350)));
    }

    private static String milestoneDescription(RepositoryEvidence source) {
        if (!source.releases().isEmpty()) {
            return countDescription(source.releases().size(), "published GitHub Release", "published GitHub Releases");
        }
        if (!source.tags().isEmpty()) {
            return countDescription(source.tags().size(), "repository tag", "repository tags");
        }
        return "No GitHub Release or repository tag was observed; the default branch anchors this foundation campaign.";
    }

    private static List<Quest> quests(RepositoryEvidence source) {
        var quests = new ArrayList<Quest>();
        source.issues().forEach(issue -> quests.add(new Quest(
                "issue-" + issue.number(),
                "quest-board",
                "verified",
                issue.title(),
                "Open GitHub issue presented as a candidate; no completion is inferred.",
                "candidate",
                "GitHub issue #" + issue.number(),
                issue.url())));
        source.roadmapItems().forEach(item -> quests.add(new Quest(
                "roadmap-" + item.id(),
                "quest-board",
                "repository-authored",
                item.title(),
                item.summary(),
                "candidate",
                item.sourceLabel(),
                item.url())));
        return List.copyOf(quests);
    }

    private static List<Encounter> encounters(RepositoryEvidence source) {
        if (!source.pullRequests().isEmpty()) {
            return source.pullRequests().stream()
                    .map(pullRequest -> new Encounter(
                            "pull-request-" + pullRequest.number(),
                            "verified",
                            "pull-request",
                            "PR #" + pullRequest.number(),
                            pullRequest.title(),
                            "Merged pull request verified by GitHub" + dateSuffix(pullRequest.mergedAt()),
                            "victory",
                            pullRequest.url()))
                    .toList();
        }
        return source.commits().stream()
                .limit(5)
                .map(commit -> new Encounter(
                        "commit-" + commit.sha(),
                        "inferred",
                        "commit",
                        commit.sha().substring(0, Math.min(7, commit.sha().length())),
                        commit.title(),
                        "Recent default-branch commit presented as an inferred expedition" + dateSuffix(commit.committedAt()),
                        "observed",
                        commit.url()))
                .toList();
    }

    private static List<Chapter> chapters(RepositoryEvidence source) {
        var chapters = new ArrayList<Chapter>();
        if (!source.releases().isEmpty()) {
            IntStream.range(0, source.releases().size()).forEach(index -> {
                var release = source.releases().get(index);
                chapters.add(new Chapter(
                        release.tag(),
                        "verified",
                        "release",
                        release.name().isBlank() ? "Release " + release.tag() : release.name(),
                        "Published GitHub Release" + dateSuffix(release.publishedAt()),
                        index == 0 ? "current" : "unlocked",
                        release.url()));
            });
        } else if (!source.tags().isEmpty()) {
            IntStream.range(0, source.tags().size()).forEach(index -> {
                var tag = source.tags().get(index);
                chapters.add(new Chapter(
                        tag.name(),
                        "repository-authored",
                        "tag",
                        "Tagged milestone " + tag.name(),
                        "Repository tag observed; GitHub Release publication is not inferred.",
                        index == 0 ? "current" : "unlocked",
                        tag.url()));
            });
        } else {
            chapters.add(new Chapter(
                    source.defaultBranch(),
                    "inferred",
                    "foundation",
                    "Foundation snapshot",
                    "No GitHub Release or tag was observed; this chapter represents the current default branch only.",
                    "current",
                    source.repository().webUrl() + "/tree/" + source.defaultBranch()));
        }
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
