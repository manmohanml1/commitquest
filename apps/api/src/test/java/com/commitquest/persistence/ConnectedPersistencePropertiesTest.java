package com.commitquest.persistence;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ConnectedPersistencePropertiesTest {

    @Test
    void acceptsACompleteBoundedConfiguration() {
        var properties = new ConnectedPersistenceProperties(true, "jdbc:postgresql://db/commitquest", "user", "secret", 3);

        assertThatCode(properties::requireCompleteConfiguration).doesNotThrowAnyException();
    }

    @Test
    void rejectsPartialOrUnboundedConfiguration() {
        assertThatThrownBy(() -> new ConnectedPersistenceProperties(true, "", "user", "secret", 3)
                        .requireCompleteConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("URL");
        assertThatThrownBy(() -> new ConnectedPersistenceProperties(true, "jdbc:postgresql://db/cq", "", "secret", 3)
                        .requireCompleteConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("username");
        assertThatThrownBy(() -> new ConnectedPersistenceProperties(true, "jdbc:postgresql://db/cq", "user", "", 3)
                        .requireCompleteConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("password");
        assertThatThrownBy(() -> new ConnectedPersistenceProperties(true, "url", "user", "secret", 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 10");
    }
}
