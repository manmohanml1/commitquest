package com.commitquest.preview.application;

import com.commitquest.preview.domain.CampaignProjection;
import com.commitquest.preview.domain.RepositoryEvidence;
import com.commitquest.preview.domain.RepositoryRef;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public final class RepositoryPreviewService {

    private final RepositoryUrlParser urlParser;
    private final RepositoryEvidencePort evidencePort;
    private final CampaignProjectionMapper projectionMapper;
    private final ConcurrentHashMap<String, CachedEvidence> cache = new ConcurrentHashMap<>();
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final int CACHE_LIMIT = 256;

    public RepositoryPreviewService(
            RepositoryUrlParser urlParser,
            RepositoryEvidencePort evidencePort,
            CampaignProjectionMapper projectionMapper) {
        this.urlParser = urlParser;
        this.evidencePort = evidencePort;
        this.projectionMapper = projectionMapper;
    }

    public CampaignProjection preview(String repositoryUrl) {
        var repository = urlParser.parse(repositoryUrl);
        return projectionMapper.map(load(repository));
    }

    private RepositoryEvidence load(RepositoryRef repository) {
        var now = Instant.now();
        var cached = cache.get(repository.fullName());
        if (cached != null && cached.expiresAt().isAfter(now)) return cached.evidence();
        var evidence = evidencePort.load(repository);
        evictBeforeInsert(now);
        cache.put(repository.fullName(), new CachedEvidence(evidence, now.plus(CACHE_TTL)));
        return evidence;
    }

    private void evictBeforeInsert(Instant now) {
        cache.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        if (cache.size() < CACHE_LIMIT) return;
        var candidate = cache.keys();
        if (candidate.hasMoreElements()) cache.remove(candidate.nextElement());
    }

    private record CachedEvidence(RepositoryEvidence evidence, Instant expiresAt) {}
}
