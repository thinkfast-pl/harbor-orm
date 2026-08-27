// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain.fixtures;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.test.domain.BasicEntity;
import io.github.thinkfastpl.harbororm.test.domain.BasicsByteaEntity;
import io.github.thinkfastpl.harbororm.test.domain.ProductEntity;
import io.github.thinkfastpl.harbororm.test.domain.UserEntity;
import io.github.thinkfastpl.harbororm.test.domain.repository.BasicRepository;
import io.github.thinkfastpl.harbororm.test.domain.repository.BasicsByteaRepository;
import io.github.thinkfastpl.harbororm.test.domain.repository.ProductRepository;
import io.github.thinkfastpl.harbororm.test.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public class TestFixtures {
    private final HarborSession session;

    private final BasicRepository basicRepository;
    private final BasicsByteaRepository basicsByteaRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public TestFixtures(HarborSession session) {
        this.session = session;
        this.basicRepository = new BasicRepository(session);
        this.basicsByteaRepository = new BasicsByteaRepository(session);
        this.userRepository = new UserRepository(session);
        this.productRepository = new ProductRepository(session);
    }

    public void addBasic(Long id, String name, int numero) {
        basicRepository.insert(new BasicEntity(id, name, numero));
    }

    public void addBasicBytea() {
        BasicsByteaEntity entity = new BasicsByteaEntity(
                1L,
                "A",
                new byte[]{(byte) 1, (byte) 2, (byte) 3}
        );
        basicsByteaRepository.insert(entity);
    }

    public void addBasicBytea(Long id, String name) {
        BasicsByteaEntity entity = new BasicsByteaEntity(
                id,
                name,
                new byte[]{(byte) 1, (byte) 2, (byte) 3}
        );
        basicsByteaRepository.insert(entity);
    }


    public UUID addUser() {
        UUID id = UUID.randomUUID();
        userRepository.insert(
                new UserEntity(id, "test@example.com", "pwd", "First name", "Last name", "555444333")
        );
        return id;
    }

    public Long addProductA() {
        ProductEntity productEntity = new ProductEntity(
                null,
                "A",
                new BigDecimal("1.00"),
                new BigDecimal("23"),
                new BigDecimal("1.23")
        );
        productRepository.insert(productEntity);
        return productEntity.getId();
    }

    public Long addProductB() {
        ProductEntity productEntity = new ProductEntity(
                null,
                "B",
                new BigDecimal("1.50"),
                new BigDecimal("23"),
                new BigDecimal("1.85")
        );
        productRepository.insert(productEntity);
        return productEntity.getId();
    }
}
