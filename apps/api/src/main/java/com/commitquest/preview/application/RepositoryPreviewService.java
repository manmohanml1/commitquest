package com.commitquest.preview.application;

import com.commitquest.preview.domain.CampaignProjection;
import org.springframework.stereotype.Service;

@Service
public final class RepositoryPreviewService {

    private final RepositoryUrlParser urlParser;
    private final RepositoryEvidencePort evidencePort;
    private final CampaignProjectionMapper projectionMapper;

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
        return projectionMapper.map(evidencePort.load(repository));
    }
}
