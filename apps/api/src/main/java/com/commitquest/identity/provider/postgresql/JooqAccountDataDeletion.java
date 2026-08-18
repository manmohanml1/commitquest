package com.commitquest.identity.provider.postgresql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.commitquest.identity.application.AccountDataDeletion;
import com.commitquest.identity.domain.AccountId;
import java.util.Objects;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;

public final class JooqAccountDataDeletion implements AccountDataDeletion {

    private static final Table<?> ACCOUNT = table(name("cq_account"));
    private static final Field<UUID> ID = field(name("id"), UUID.class);

    private final DSLContext dsl;

    public JooqAccountDataDeletion(DSLContext dsl) {
        this.dsl = Objects.requireNonNull(dsl);
    }

    @Override
    public boolean delete(AccountId accountId) {
        Objects.requireNonNull(accountId, "Account ID is required.");
        return dsl.deleteFrom(ACCOUNT).where(ID.eq(accountId.value())).execute() == 1;
    }
}
