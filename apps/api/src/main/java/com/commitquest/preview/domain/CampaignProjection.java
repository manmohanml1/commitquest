package com.commitquest.preview.domain;

import java.util.List;

public record CampaignProjection(
        int schemaVersion,
        int mappingAlgorithmVersion,
        int scoringRulesetVersion,
        String slug,
        String title,
        String repository,
        String mode,
        String currentChapter,
        List<Metric> metrics,
        List<Region> evidence,
        List<Quest> quests,
        List<Encounter> encounters,
        List<Chapter> chapters) {

    public static final int SCHEMA_VERSION = 3;
    public static final int MAPPING_ALGORITHM_VERSION = 6;
    public static final int SCORING_RULESET_VERSION = 1;

    public CampaignProjection {
        metrics = List.copyOf(metrics);
        evidence = List.copyOf(evidence);
        quests = List.copyOf(quests);
        encounters = List.copyOf(encounters);
        chapters = List.copyOf(chapters);
    }

    public record Metric(String value, String label) {}

    public record Position(int x, int y) {}

    public record Region(
            String id,
            String level,
            String eyebrow,
            String title,
            String description,
            String meta,
            String evidenceUrl,
            String status,
            String tone,
            String icon,
            Position position) {}

    public record Quest(
            String id,
            String regionId,
            String level,
            String title,
            String summary,
            String status,
            String sourceLabel,
            String evidenceUrl) {}

    public record Encounter(
            String id,
            String level,
            String kind,
            String reference,
            String title,
            String summary,
            String status,
            String evidenceUrl) {}

    public record Chapter(
            String version,
            String level,
            String kind,
            String title,
            String summary,
            String status,
            String evidenceUrl) {}
}
