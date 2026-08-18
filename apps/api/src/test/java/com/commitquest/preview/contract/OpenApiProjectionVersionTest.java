package com.commitquest.preview.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.commitquest.preview.domain.CampaignProjection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class OpenApiProjectionVersionTest {

    private static final Path OPEN_API_SPEC = Path.of("..", "..", "docs", "api", "openapi.yaml").normalize();

    @Test
    void projectionVersionConstantsMatchThePublishedContract() throws IOException {
        var contract = Files.readString(OPEN_API_SPEC);

        assertThat(integerConstant(contract, "schemaVersion")).isEqualTo(CampaignProjection.SCHEMA_VERSION);
        assertThat(integerConstant(contract, "mappingAlgorithmVersion"))
                .isEqualTo(CampaignProjection.MAPPING_ALGORITHM_VERSION);
        assertThat(integerConstant(contract, "scoringRulesetVersion"))
                .isEqualTo(CampaignProjection.SCORING_RULESET_VERSION);
    }

    private static int integerConstant(String contract, String propertyName) {
        var pattern = Pattern.compile(
                "(?m)^\\s+" + Pattern.quote(propertyName) + ":\\R\\s+type: integer\\R\\s+const: (\\d+)$");
        var matcher = pattern.matcher(contract);
        assertThat(matcher.find()).as("OpenAPI integer constant %s", propertyName).isTrue();
        return Integer.parseInt(matcher.group(1));
    }
}
