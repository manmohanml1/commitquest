package com.commitquest;

import static org.assertj.core.api.Assertions.assertThat;

import com.commitquest.identity.application.IdentityService;
import com.commitquest.identity.application.IdentityStore;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CommitQuestApiApplicationTest {

    private final ApplicationContext context;

    CommitQuestApiApplicationTest(ApplicationContext context) {
        this.context = context;
    }

    @Test
    void applicationContextStartsWithTheDefaultEphemeralConfiguration() {
        assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
        assertThat(context.getBeansOfType(Flyway.class)).isEmpty();
        assertThat(context.getBeansOfType(DSLContext.class)).isEmpty();
        assertThat(context.getBeansOfType(IdentityService.class)).isEmpty();
        assertThat(context.getBeansOfType(IdentityStore.class)).isEmpty();
    }
}
