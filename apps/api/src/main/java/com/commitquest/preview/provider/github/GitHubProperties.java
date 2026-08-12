package com.commitquest.preview.provider.github;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("commitquest.github")
public record GitHubProperties(
        String baseUrl, String token, Duration connectTimeout, Duration readTimeout) {}
