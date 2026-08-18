package com.commitquest.campaign.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commitquest.campaign.application.SavedCampaignExport;
import com.commitquest.campaign.application.SavedCampaignNotFound;
import com.commitquest.campaign.application.SavedCampaignService;
import com.commitquest.campaign.domain.CampaignVisibility;
import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.identity.web.BrowserAuthentication;
import com.commitquest.preview.domain.CampaignProjection;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SavedCampaignControllerTest {

    private static final AccountId OWNER = new AccountId(new UUID(0, 1));
    private static final UUID CAMPAIGN_ID = new UUID(0, 2);
    private SavedCampaignService service;
    private BrowserAuthentication authentication;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(SavedCampaignService.class);
        authentication = mock(BrowserAuthentication.class);
        mvc = MockMvcBuilders.standaloneSetup(new SavedCampaignController(service, authentication))
                .setControllerAdvice(new SavedCampaignExceptionHandler())
                .build();
    }

    @Test
    void savesThroughMutationAuthenticationAndNeverExposesTheOwnerId() throws Exception {
        when(authentication.requireMutation("session", "csrf", "csrf", "https://commitquest.example", null))
                .thenReturn(OWNER);
        when(service.save(OWNER, "https://github.com/owner/repository")).thenReturn(campaign());

        var response = mvc.perform(post("/api/v1/campaigns")
                        .cookie(new Cookie(BrowserAuthentication.SESSION_COOKIE, "session"))
                        .cookie(new Cookie(BrowserAuthentication.CSRF_COOKIE, "csrf"))
                        .header(BrowserAuthentication.CSRF_HEADER, "csrf")
                        .header(HttpHeaders.ORIGIN, "https://commitquest.example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryUrl\":\"https://github.com/owner/repository\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CAMPAIGN_ID.toString()))
                .andExpect(jsonPath("$.projection.repository").value("owner/repository"))
                .andExpect(jsonPath("$.visibility").value("private"))
                .andReturn();

        assertThat(response.getResponse().getContentAsString()).doesNotContain("ownerId");
    }

    @Test
    void listsOnlyAfterReadAuthenticationAndDeletesOnlyAfterMutationAuthentication() throws Exception {
        when(authentication.requireAccount("session")).thenReturn(OWNER);
        when(authentication.requireMutation("session", "csrf", "csrf", "https://commitquest.example", null))
                .thenReturn(OWNER);
        when(service.list(OWNER)).thenReturn(List.of(campaign()));

        mvc.perform(get("/api/v1/campaigns")
                        .cookie(new Cookie(BrowserAuthentication.SESSION_COOKIE, "session")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(CAMPAIGN_ID.toString()));

        mvc.perform(delete("/api/v1/campaigns/{campaignId}", CAMPAIGN_ID)
                        .cookie(new Cookie(BrowserAuthentication.SESSION_COOKIE, "session"))
                        .cookie(new Cookie(BrowserAuthentication.CSRF_COOKIE, "csrf"))
                        .header(BrowserAuthentication.CSRF_HEADER, "csrf")
                        .header(HttpHeaders.ORIGIN, "https://commitquest.example"))
                .andExpect(status().isNoContent());

        verify(service).delete(OWNER, new SavedCampaignId(CAMPAIGN_ID));
    }

    @Test
    void returnsTheSameNotFoundShapeForMissingOrCrossOwnerCampaigns() throws Exception {
        when(authentication.requireAccount("session")).thenReturn(OWNER);
        when(service.get(OWNER, new SavedCampaignId(CAMPAIGN_ID))).thenThrow(new SavedCampaignNotFound());

        mvc.perform(get("/api/v1/campaigns/{campaignId}", CAMPAIGN_ID)
                        .cookie(new Cookie(BrowserAuthentication.SESSION_COOKIE, "session")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SAVED_CAMPAIGN_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("The saved campaign was not found."));
    }

    @Test
    void refreshesFromTheStoredRepositoryInsteadOfAcceptingAReplacementUrl() throws Exception {
        allowMutation();
        when(service.get(OWNER, new SavedCampaignId(CAMPAIGN_ID))).thenReturn(campaign());
        when(service.save(OWNER, "https://github.com/owner/repository")).thenReturn(campaign());

        mvc.perform(post("/api/v1/campaigns/{campaignId}/refresh", CAMPAIGN_ID)
                        .cookie(new Cookie(BrowserAuthentication.SESSION_COOKIE, "session"))
                        .cookie(new Cookie(BrowserAuthentication.CSRF_COOKIE, "csrf"))
                        .header(BrowserAuthentication.CSRF_HEADER, "csrf")
                        .header(HttpHeaders.ORIGIN, "https://commitquest.example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projection.repository").value("owner/repository"));

        verify(service).save(OWNER, "https://github.com/owner/repository");
    }

    @Test
    void changesVisibilityAndExportsAStableVersionedDocument() throws Exception {
        allowMutation();
        when(authentication.requireAccount("session")).thenReturn(OWNER);
        when(service.changeVisibility(OWNER, new SavedCampaignId(CAMPAIGN_ID), CampaignVisibility.UNLISTED))
                .thenReturn(campaign().changeVisibility(CampaignVisibility.UNLISTED, Instant.parse("2026-08-17T13:00:00Z")));
        when(service.export(OWNER, new SavedCampaignId(CAMPAIGN_ID)))
                .thenReturn(new SavedCampaignExport(
                        1,
                        CAMPAIGN_ID,
                        "owner/repository",
                        "Repository Keep",
                        "unlisted",
                        CampaignProjection.SCHEMA_VERSION,
                        CampaignProjection.MAPPING_ALGORITHM_VERSION,
                        CampaignProjection.SCORING_RULESET_VERSION,
                        Instant.parse("2026-08-17T12:00:00Z"),
                        Instant.parse("2026-08-17T13:00:00Z"),
                        projection()));

        mvc.perform(patch("/api/v1/campaigns/{campaignId}/visibility", CAMPAIGN_ID)
                        .cookie(new Cookie(BrowserAuthentication.SESSION_COOKIE, "session"))
                        .cookie(new Cookie(BrowserAuthentication.CSRF_COOKIE, "csrf"))
                        .header(BrowserAuthentication.CSRF_HEADER, "csrf")
                        .header(HttpHeaders.ORIGIN, "https://commitquest.example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visibility\":\"UNLISTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("unlisted"));

        mvc.perform(get("/api/v1/campaigns/{campaignId}/export", CAMPAIGN_ID)
                        .cookie(new Cookie(BrowserAuthentication.SESSION_COOKIE, "session")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exportSchemaVersion").value(1))
                .andExpect(jsonPath("$.mappingAlgorithmVersion")
                        .value(CampaignProjection.MAPPING_ALGORITHM_VERSION))
                .andExpect(jsonPath("$.projection.repository").value("owner/repository"));
    }

    private void allowMutation() {
        when(authentication.requireMutation("session", "csrf", "csrf", "https://commitquest.example", null))
                .thenReturn(OWNER);
    }

    private static SavedCampaign campaign() {
        var now = Instant.parse("2026-08-17T12:00:00Z");
        return SavedCampaign.create(new SavedCampaignId(CAMPAIGN_ID), OWNER, projection(), now);
    }

    private static CampaignProjection projection() {
        return new CampaignProjection(
                CampaignProjection.SCHEMA_VERSION,
                CampaignProjection.MAPPING_ALGORITHM_VERSION,
                CampaignProjection.SCORING_RULESET_VERSION,
                "owner-repository",
                "Repository Keep",
                "owner/repository",
                "foundation",
                "main",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }
}
