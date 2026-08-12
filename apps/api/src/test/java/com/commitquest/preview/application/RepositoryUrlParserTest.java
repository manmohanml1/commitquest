package com.commitquest.preview.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RepositoryUrlParserTest {

    private final RepositoryUrlParser parser = new RepositoryUrlParser();

    @Test
    void parsesACanonicalPublicGitHubUrl() {
        var repository = parser.parse("https://github.com/manmohanml1/commitquest");

        assertThat(repository.owner()).isEqualTo("manmohanml1");
        assertThat(repository.name()).isEqualTo("commitquest");
    }

    @Test
    void acceptsTheGitCloneSuffixWithoutLeakingItIntoIdentity() {
        assertThat(parser.parse("https://github.com/openai/openai-java.git").name())
                .isEqualTo("openai-java");
    }

    @Test
    void rejectsNonGitHubAndAmbiguousUrls() {
        assertThatThrownBy(() -> parser.parse("http://github.com/owner/repository"))
                .isInstanceOf(PreviewFailure.class)
                .extracting(failure -> ((PreviewFailure) failure).code())
                .isEqualTo(PreviewFailure.Code.INVALID_REPOSITORY_URL);
        assertThatThrownBy(() -> parser.parse("https://github.com/owner/repository/issues"))
                .isInstanceOf(PreviewFailure.class);
        assertThatThrownBy(() -> parser.parse("https://example.com/owner/repository"))
                .isInstanceOf(PreviewFailure.class);
    }
}
