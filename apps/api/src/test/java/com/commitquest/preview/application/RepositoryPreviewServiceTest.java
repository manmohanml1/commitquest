package com.commitquest.preview.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.commitquest.preview.domain.RepositoryEvidence;
import com.commitquest.preview.domain.RepositoryRef;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RepositoryPreviewServiceTest {

    @Test
    void reusesBoundedProviderEvidenceWithinTheCacheWindow() {
        var loads = new AtomicInteger();
        RepositoryEvidencePort port = repository -> {
            loads.incrementAndGet();
            return evidence(repository);
        };
        var service = new RepositoryPreviewService(
                new RepositoryUrlParser(), port, new CampaignProjectionMapper());

        service.preview("https://github.com/owner/repository");
        service.preview("https://github.com/owner/repository");

        assertThat(loads).hasValue(1);
    }

    private static RepositoryEvidence evidence(RepositoryRef repository) {
        return new RepositoryEvidence(
                repository,
                "Cached repository",
                "GitHub repository metadata",
                "main",
                "Java",
                false,
                "2026-08-11T12:00:00Z",
                List.of("README.md"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new RepositoryEvidence.Commit(
                        "abcdef012345",
                        "Initial",
                        repository.webUrl() + "/commit/abcdef012345",
                        "2026-08-11T12:00:00Z")));
    }
}
