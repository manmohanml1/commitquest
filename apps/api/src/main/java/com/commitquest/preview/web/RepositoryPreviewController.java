package com.commitquest.preview.web;

import com.commitquest.preview.application.RepositoryPreviewService;
import com.commitquest.preview.domain.CampaignProjection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repository-previews")
final class RepositoryPreviewController {

    private final RepositoryPreviewService previewService;

    RepositoryPreviewController(RepositoryPreviewService previewService) {
        this.previewService = previewService;
    }

    @PostMapping
    CampaignProjection create(@Valid @RequestBody PreviewRequest request) {
        return previewService.preview(request.repositoryUrl());
    }

    record PreviewRequest(
            @NotBlank(message = "Repository URL is required.")
                    @Size(max = 2048, message = "Repository URL is too long.")
                    String repositoryUrl) {}
}
