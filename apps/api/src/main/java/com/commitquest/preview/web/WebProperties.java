package com.commitquest.preview.web;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("commitquest.web")
record WebProperties(List<String> allowedOriginPatterns) {

    WebProperties {
        allowedOriginPatterns = List.copyOf(allowedOriginPatterns);
    }
}
