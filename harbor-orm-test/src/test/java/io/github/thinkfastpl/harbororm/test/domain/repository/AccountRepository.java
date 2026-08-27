// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.repository;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.core.repository.EntityRepository;
import io.github.thinkfastpl.harbororm.test.domain.AccountEntity;
import io.github.thinkfastpl.harbororm.test.domain.QAccountEntity;
import lombok.NonNull;

public class AccountRepository extends EntityRepository<AccountEntity, Long> {

    public AccountRepository(@NonNull HarborSession session) {
        super(session, new QAccountEntity(null));
    }
}
