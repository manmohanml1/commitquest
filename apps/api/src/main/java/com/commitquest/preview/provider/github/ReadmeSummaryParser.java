package com.commitquest.preview.provider.github;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
final class ReadmeSummaryParser {

    private static final int MAX_SUMMARY_LENGTH = 320;
    private static final Pattern IMAGE = Pattern.compile("!\\[[^]]*]\\([^)]*\\)");
    private static final Pattern LINK = Pattern.compile("\\[([^]]+)]\\([^)]*\\)");
    private static final Pattern HTML = Pattern.compile("<[^>]+>");
    private static final Pattern DECORATION = Pattern.compile("[*_`~]");
    private static final Pattern TABLE_DIVIDER = Pattern.compile("^\\|?[ :|-]+\\|?$");

    String parse(String markdown) {
        if (markdown == null || markdown.isBlank()) return "";

        var summary = new StringBuilder();
        var inFence = false;
        var inFrontMatter = false;
        var firstContentSeen = false;

        for (var rawLine : markdown.replace("\r", "").split("\n")) {
            var line = rawLine.trim();
            if (!firstContentSeen && line.isBlank()) continue;
            if (!firstContentSeen && line.equals("---")) {
                firstContentSeen = true;
                inFrontMatter = true;
                continue;
            }
            firstContentSeen = true;
            if (inFrontMatter) {
                if (line.equals("---")) inFrontMatter = false;
                continue;
            }
            if (line.startsWith("```") || line.startsWith("~~~")) {
                inFence = !inFence;
                continue;
            }
            if (inFence) continue;
            if (line.isBlank()) {
                if (!summary.isEmpty()) break;
                continue;
            }
            if (isStructural(line)) {
                if (!summary.isEmpty()) break;
                continue;
            }

            var text = plainText(line);
            if (text.isBlank()) continue;
            if (!summary.isEmpty()) summary.append(' ');
            summary.append(text);
            if (summary.length() >= MAX_SUMMARY_LENGTH) break;
        }

        return truncate(summary.toString().replaceAll("\\s+", " ").trim());
    }

    private static boolean isStructural(String line) {
        return line.startsWith("#")
                || line.startsWith("- ")
                || line.startsWith("* ")
                || line.startsWith("+ ")
                || line.matches("^\\d+[.)]\\s+.*")
                || line.startsWith(">")
                || line.startsWith("[!")
                || line.startsWith("![")
                || TABLE_DIVIDER.matcher(line).matches();
    }

    private static String plainText(String line) {
        var text = IMAGE.matcher(line).replaceAll("");
        text = LINK.matcher(text).replaceAll("$1");
        text = HTML.matcher(text).replaceAll(" ");
        text = DECORATION.matcher(text).replaceAll("");
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String truncate(String value) {
        if (value.length() <= MAX_SUMMARY_LENGTH) return value;
        var boundary = value.lastIndexOf(' ', MAX_SUMMARY_LENGTH - 1);
        return value.substring(0, boundary > 0 ? boundary : MAX_SUMMARY_LENGTH).stripTrailing() + "…";
    }
}
