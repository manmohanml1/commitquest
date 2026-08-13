package com.commitquest.preview.provider.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoadmapParserTest {

    private final RoadmapParser parser = new RoadmapParser();

    @Test
    void readsBulletsAndOnlyUnshippedTableRows() {
        var items = parser.parse(
                """
                # Roadmap
                - Add keyboard guide.
                - Complete the accessibility audit.

                | Addition | Purpose | Status |
                | --- | --- | --- |
                | Existing system | Already available | Shipped in v1.0.0 |
                | Public sharing | Publish a safe view | Planned |

                ## Delivery Notes
                - This explains the current implementation and is not planned work.
                """,
                "https://github.com/owner/repository/blob/main/ROADMAP.md");

        assertThat(items).extracting(item -> item.title())
                .containsExactly("Add keyboard guide.", "Complete the accessibility audit.", "Public sharing");
        assertThat(items.get(2).summary()).contains("Publish a safe view", "Planned");
    }
}
