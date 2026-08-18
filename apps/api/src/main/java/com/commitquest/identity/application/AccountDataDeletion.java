package com.commitquest.identity.application;

import com.commitquest.identity.domain.AccountId;

public interface AccountDataDeletion {

    boolean delete(AccountId accountId);
}
