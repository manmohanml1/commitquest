package com.commitquest.identity.provider;

import com.commitquest.identity.application.AccountDataDeletion;
import com.commitquest.identity.application.IdentityService;
import com.commitquest.identity.application.IdentityStore;
import com.commitquest.identity.application.OAuthIdentityGateway;
import com.commitquest.identity.application.TokenSecurity;
import com.commitquest.identity.provider.github.GitHubOAuthGateway;
import com.commitquest.identity.provider.github.IdentityProperties;
import com.commitquest.identity.provider.postgresql.JooqIdentityStore;
import com.commitquest.identity.provider.security.HmacTokenSecurity;
import java.time.Clock;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "commitquest.identity", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(IdentityProperties.class)
public class IdentityConfiguration {

    @Bean
    TokenSecurity identityTokenSecurity(IdentityProperties properties) {
        properties.requireCompleteConfiguration();
        return new HmacTokenSecurity(properties.secretBytes());
    }

    @Bean
    IdentityStore identityStore(DSLContext dsl) {
        return new JooqIdentityStore(dsl);
    }

    @Bean
    OAuthIdentityGateway oauthIdentityGateway(IdentityProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        var tokenClient = RestClient.builder().requestFactory(requestFactory).build();
        var apiClient = RestClient.builder()
                .baseUrl(properties.apiBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "CommitQuest/0.4")
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10")
                .build();
        return new GitHubOAuthGateway(properties, tokenClient, apiClient);
    }

    @Bean
    IdentityService identityService(
            IdentityStore store,
            OAuthIdentityGateway gateway,
            TokenSecurity tokenSecurity,
            AccountDataDeletion accountDataDeletion) {
        return new IdentityService(store, gateway, tokenSecurity, accountDataDeletion, Clock.systemUTC());
    }
}
