package com.commitquest.preview.application;

import com.commitquest.preview.domain.RepositoryEvidence;
import com.commitquest.preview.domain.RepositoryRef;

public interface RepositoryEvidencePort {

    RepositoryEvidence load(RepositoryRef repository);
}
