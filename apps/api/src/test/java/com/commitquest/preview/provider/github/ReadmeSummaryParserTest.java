package com.commitquest.preview.provider.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReadmeSummaryParserTest {

    private final ReadmeSummaryParser parser = new ReadmeSummaryParser();

    @Test
    void extractsTheFirstNarrativeParagraphAndStripsInlineMarkdown() {
        var summary = parser.parse("""
                # Novel Browser Glass

                [![Quality](https://example.test/badge.svg)](https://example.test)

                Glasses-first **novel reader** for [Meta Ray-Ban Display](https://example.test),
                with saved progress and D-pad navigation.

                - Search novels
                - Read chapters
                """);

        assertThat(summary).isEqualTo(
                "Glasses-first novel reader for Meta Ray-Ban Display, with saved progress and D-pad navigation.");
    }

    @Test
    void ignoresFrontMatterCodeAndListsWhenNoNarrativeParagraphExists() {
        var summary = parser.parse("""
                ---
                title: Example
                ---
                # Example
                ```text
                not a description
                ```
                - Feature one
                - Feature two
                """);

        assertThat(summary).isEmpty();
    }
}
