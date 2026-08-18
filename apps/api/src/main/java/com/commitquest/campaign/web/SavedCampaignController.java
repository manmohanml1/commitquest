package com.commitquest.campaign.web;

import com.commitquest.campaign.application.SavedCampaignExport;
import com.commitquest.campaign.application.SavedCampaignService;
import com.commitquest.campaign.domain.CampaignVisibility;
import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.identity.web.BrowserAuthentication;
import com.commitquest.preview.domain.CampaignProjection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campaigns")
@ConditionalOnProperty(prefix = "commitquest.identity", name = "enabled", havingValue = "true")
final class SavedCampaignController {

    private final SavedCampaignService campaigns;
    private final BrowserAuthentication authentication;

    SavedCampaignController(SavedCampaignService campaigns, BrowserAuthentication authentication) {
        this.campaigns = campaigns;
        this.authentication = authentication;
    }

    @PostMapping
    SavedCampaignView save(
            @Valid @RequestBody SaveCampaignRequest request,
            @CookieValue(name = BrowserAuthentication.SESSION_COOKIE, required = false) String session,
            @CookieValue(name = BrowserAuthentication.CSRF_COOKIE, required = false) String csrfCookie,
            @RequestHeader(name = BrowserAuthentication.CSRF_HEADER, required = false) String csrfHeader,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
            @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer) {
        var owner = authentication.requireMutation(session, csrfCookie, csrfHeader, origin, referer);
        return SavedCampaignView.from(campaigns.save(owner, request.repositoryUrl()));
    }

    @GetMapping
    List<SavedCampaignView> list(
            @CookieValue(name = BrowserAuthentication.SESSION_COOKIE, required = false) String session) {
        return campaigns.list(authentication.requireAccount(session)).stream()
                .map(SavedCampaignView::from)
                .toList();
    }

    @GetMapping("/{campaignId}")
    SavedCampaignView get(
            @PathVariable UUID campaignId,
            @CookieValue(name = BrowserAuthentication.SESSION_COOKIE, required = false) String session) {
        return SavedCampaignView.from(
                campaigns.get(authentication.requireAccount(session), new SavedCampaignId(campaignId)));
    }

    @PostMapping("/{campaignId}/refresh")
    SavedCampaignView refresh(
            @PathVariable UUID campaignId,
            @CookieValue(name = BrowserAuthentication.SESSION_COOKIE, required = false) String session,
            @CookieValue(name = BrowserAuthentication.CSRF_COOKIE, required = false) String csrfCookie,
            @RequestHeader(name = BrowserAuthentication.CSRF_HEADER, required = false) String csrfHeader,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
            @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer) {
        var owner = authentication.requireMutation(session, csrfCookie, csrfHeader, origin, referer);
        var existing = campaigns.get(owner, new SavedCampaignId(campaignId));
        return SavedCampaignView.from(campaigns.save(
                owner, "https://github.com/" + existing.projection().repository()));
    }

    @PatchMapping("/{campaignId}/visibility")
    SavedCampaignView changeVisibility(
            @PathVariable UUID campaignId,
            @Valid @RequestBody ChangeVisibilityRequest request,
            @CookieValue(name = BrowserAuthentication.SESSION_COOKIE, required = false) String session,
            @CookieValue(name = BrowserAuthentication.CSRF_COOKIE, required = false) String csrfCookie,
            @RequestHeader(name = BrowserAuthentication.CSRF_HEADER, required = false) String csrfHeader,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
            @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer) {
        var owner = authentication.requireMutation(session, csrfCookie, csrfHeader, origin, referer);
        return SavedCampaignView.from(campaigns.changeVisibility(
                owner, new SavedCampaignId(campaignId), request.visibility()));
    }

    @GetMapping("/{campaignId}/export")
    SavedCampaignExport export(
            @PathVariable UUID campaignId,
            @CookieValue(name = BrowserAuthentication.SESSION_COOKIE, required = false) String session) {
        return campaigns.export(authentication.requireAccount(session), new SavedCampaignId(campaignId));
    }

    @DeleteMapping("/{campaignId}")
    ResponseEntity<Void> delete(
            @PathVariable UUID campaignId,
            @CookieValue(name = BrowserAuthentication.SESSION_COOKIE, required = false) String session,
            @CookieValue(name = BrowserAuthentication.CSRF_COOKIE, required = false) String csrfCookie,
            @RequestHeader(name = BrowserAuthentication.CSRF_HEADER, required = false) String csrfHeader,
            @RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
            @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer) {
        var owner = authentication.requireMutation(session, csrfCookie, csrfHeader, origin, referer);
        campaigns.delete(owner, new SavedCampaignId(campaignId));
        return ResponseEntity.noContent().build();
    }

    record SaveCampaignRequest(
            @NotBlank(message = "A GitHub repository URL is required.")
            @Size(max = 2048, message = "The repository URL is too long.")
            String repositoryUrl) {}

    record ChangeVisibilityRequest(@NotNull CampaignVisibility visibility) {}

    record SavedCampaignView(
            UUID id,
            CampaignProjection projection,
            String visibility,
            Instant createdAt,
            Instant updatedAt) {

        static SavedCampaignView from(SavedCampaign campaign) {
            return new SavedCampaignView(
                    campaign.id().value(),
                    campaign.projection(),
                    campaign.visibility().name().toLowerCase(Locale.ROOT),
                    campaign.createdAt(),
                    campaign.updatedAt());
        }
    }
}
