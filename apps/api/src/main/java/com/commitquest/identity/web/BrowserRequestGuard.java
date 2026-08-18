package com.commitquest.identity.web;

import com.commitquest.identity.application.IdentityFailure;
import com.commitquest.preview.web.WebProperties;
import java.net.URI;
import org.springframework.util.PatternMatchUtils;

final class BrowserRequestGuard {

    private final WebProperties properties;

    BrowserRequestGuard(WebProperties properties) {
        this.properties = properties;
    }

    void requireAllowedSource(String origin, String referer) {
        var source = origin == null || origin.isBlank() ? originOf(referer) : origin;
        if (source == null
                || properties.allowedOriginPatterns().stream()
                        .noneMatch(pattern -> PatternMatchUtils.simpleMatch(pattern, source))) {
            throw new IdentityFailure(
                    IdentityFailure.Code.INVALID_CSRF, "The request origin is not allowed to change account data.");
        }
    }

    private static String originOf(String referer) {
        if (referer == null || referer.isBlank()) return null;
        try {
            var uri = URI.create(referer);
            if (uri.getScheme() == null || uri.getHost() == null) return null;
            var defaultPort = ("https".equalsIgnoreCase(uri.getScheme()) && uri.getPort() == 443)
                    || ("http".equalsIgnoreCase(uri.getScheme()) && uri.getPort() == 80);
            return uri.getScheme() + "://" + uri.getHost()
                    + (uri.getPort() < 0 || defaultPort ? "" : ":" + uri.getPort());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
