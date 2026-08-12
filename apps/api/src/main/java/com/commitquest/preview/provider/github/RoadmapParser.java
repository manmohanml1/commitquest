package com.commitquest.preview.provider.github;

import com.commitquest.preview.domain.RepositoryEvidence;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
final class RoadmapParser {

    private static final int LIMIT = 10;
    private static final Pattern BULLET = Pattern.compile("^\\s*[-*]\\s+(.+?)\\s*$");

    List<RepositoryEvidence.RoadmapItem> parse(String markdown, String sourceUrl) {
        if (markdown == null || markdown.isBlank()) {
            return List.of();
        }

        var items = new ArrayList<RepositoryEvidence.RoadmapItem>();
        var lines = markdown.lines().toList();
        var section = "";
        for (int index = 0; index < lines.size() && items.size() < LIMIT; index++) {
            var line = lines.get(index).trim();
            if (line.startsWith("#")) {
                section = line.replaceFirst("^#+\\s*", "").toLowerCase(Locale.ROOT);
                continue;
            }
            var bullet = BULLET.matcher(line);
            if (bullet.matches() && !isInformationalSection(section)) {
                addIfCandidate(
                        items,
                        bullet.group(1),
                        "Repository-authored roadmap candidate.",
                        "ROADMAP.md",
                        sourceUrl);
                continue;
            }
            if (!line.startsWith("|") || index + 1 >= lines.size() || !isSeparator(lines.get(index + 1))) {
                continue;
            }
            var headers = cells(line);
            for (index += 2; index < lines.size() && lines.get(index).trim().startsWith("|"); index++) {
                var values = cells(lines.get(index));
                addTableCandidate(items, headers, values, sourceUrl);
                if (items.size() == LIMIT) break;
            }
            index--;
        }
        return List.copyOf(items);
    }

    private static void addTableCandidate(
            List<RepositoryEvidence.RoadmapItem> items,
            List<String> headers,
            List<String> values,
            String sourceUrl) {
        if (values.isEmpty()) return;
        var statusIndex = findHeader(headers, "status");
        var status = statusIndex >= 0 && statusIndex < values.size() ? values.get(statusIndex) : "";
        if (isCompleted(status)) return;

        var titleIndex = firstHeader(headers, List.of("outcome", "addition", "feature", "item", "milestone", "version"));
        var summaryIndex = firstHeader(headers, List.of("purpose", "summary", "description", "outcome"));
        var title = value(values, titleIndex >= 0 ? titleIndex : 0);
        var summary = value(values, summaryIndex);
        if (!status.isBlank()) summary = summary.isBlank() ? "Roadmap status: " + status : summary + " · Status: " + status;
        addIfCandidate(
                items,
                title,
                summary.isBlank() ? "Repository-authored roadmap candidate." : summary,
                "ROADMAP.md",
                sourceUrl);
    }

    private static void addIfCandidate(
            List<RepositoryEvidence.RoadmapItem> items,
            String raw,
            String summary,
            String sourceLabel,
            String sourceUrl) {
        if (isCompleted(raw)) return;
        var title = clean(raw);
        if (title.isBlank()) return;
        var id = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (id.isBlank() || items.stream().anyMatch(item -> item.id().equals(id))) return;
        items.add(new RepositoryEvidence.RoadmapItem(id, title, summary, sourceLabel, sourceUrl));
    }

    private static boolean isSeparator(String line) {
        return line.replace("|", "").replace(":", "").replace("-", "").trim().isEmpty();
    }

    private static List<String> cells(String line) {
        return Pattern.compile("\\|").splitAsStream(line)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private static int firstHeader(List<String> headers, List<String> candidates) {
        return candidates.stream().mapToInt(candidate -> findHeader(headers, candidate)).filter(index -> index >= 0).findFirst().orElse(-1);
    }

    private static int findHeader(List<String> headers, String candidate) {
        for (int index = 0; index < headers.size(); index++) {
            if (headers.get(index).toLowerCase(Locale.ROOT).contains(candidate)) return index;
        }
        return -1;
    }

    private static String value(List<String> values, int index) {
        return index >= 0 && index < values.size() ? clean(values.get(index)) : "";
    }

    private static String clean(String value) {
        return value.replaceAll("^\\[[ xX]]\\s*", "")
                .replaceAll("[`*_]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean isCompleted(String value) {
        var normalized = value.toLowerCase(Locale.ROOT).trim();
        return normalized.equals("shipped")
                || normalized.startsWith("shipped in ")
                || normalized.equals("released")
                || normalized.startsWith("released in ")
                || normalized.equals("complete")
                || normalized.equals("completed")
                || normalized.equals("done")
                || normalized.matches(".*\\[[xX]](?:\\s|$).*");
    }

    private static boolean isInformationalSection(String section) {
        return section.contains("delivery note")
                || section.contains("release rule")
                || section.contains("rollout rule")
                || section.contains("contributing")
                || section.contains("principle");
    }
}
