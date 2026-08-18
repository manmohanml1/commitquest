package com.commitquest.persistence;

import com.commitquest.campaign.application.SavedCampaignStore;
import com.commitquest.campaign.provider.postgresql.JooqSavedCampaignStore;
import com.commitquest.identity.application.AccountDataDeletion;
import com.commitquest.identity.provider.postgresql.JooqAccountDataDeletion;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "commitquest.persistence", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ConnectedPersistenceProperties.class)
public class ConnectedPersistenceConfiguration {

    @Bean(destroyMethod = "close")
    HikariDataSource commitQuestDataSource(ConnectedPersistenceProperties properties) {
        properties.requireCompleteConfiguration();
        var configuration = new HikariConfig();
        configuration.setJdbcUrl(properties.url());
        configuration.setUsername(properties.username());
        configuration.setPassword(properties.password());
        configuration.setMaximumPoolSize(properties.maximumPoolSize());
        configuration.setMinimumIdle(0);
        configuration.setPoolName("commitquest-persistence");
        return new HikariDataSource(configuration);
    }

    @Bean
    Flyway commitQuestFlyway(DataSource dataSource) {
        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    DSLContext commitQuestDslContext(DataSource dataSource, Flyway commitQuestFlyway) {
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Bean
    SavedCampaignStore savedCampaignStore(DSLContext dsl, ObjectMapper objectMapper) {
        return new JooqSavedCampaignStore(dsl, objectMapper);
    }

    @Bean
    AccountDataDeletion accountDataDeletion(DSLContext dsl) {
        return new JooqAccountDataDeletion(dsl);
    }
}
