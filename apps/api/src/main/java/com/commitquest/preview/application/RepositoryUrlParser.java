package com.commitquest.preview.application;

import com.commitquest.preview.domain.RepositoryRef;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public final class RepositoryUrlParser {

    private static final Pattern SEGMENT = Pattern.compile("[A-Za-z0-9_.-]{1,100}");

    public RepositoryRef parse(String rawUrl) {
        try {
            var uri = new URI(rawUrl.strip());
            validateAuthority(uri);

            var path = uri.getPath();
            var segments = path == null ? new String[0] : path.replaceFirst("^/", "").split("/");
            if (segments.length != 2) {
                throw invalid();
            }

            var owner = segments[0];
            var repository = segments[1].endsWith(".git")
                    ? segments[1].substring(0, segments[1].length() - 4)
                    : segments[1];
            if (!validSegment(owner) || !validSegment(repository)) {
                throw invalid();
            }

            return new RepositoryRef(owner, repository);
        } catch (URISyntaxException | NullPointerException exception) {
            throw invalid();
        }
    }

    private static void validateAuthority(URI uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !"github.com".equalsIgnoreCase(uri.getHost())
                || uri.getPort() != -1
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            throw invalid();
        }
    }

    private static boolean validSegment(String value) {
        return SEGMENT.matcher(value).matches() && !".".equals(value) && !"..".equals(value);
    }

    private static PreviewFailure invalid() {
        return new PreviewFailure(
                PreviewFailure.Code.INVALID_REPOSITORY_URL,
                "Enter a public GitHub repository URL such as https://github.com/owner/repository.");
    }
}
