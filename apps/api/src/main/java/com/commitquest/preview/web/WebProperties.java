package com.commitquest.preview.web;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("commitquest.web")
public record WebProperties(List<String> allowedOriginPatterns) {

    public WebProperties {
        allowedOriginPatterns = List.copyOf(allowedOriginPatterns);
    }
}
