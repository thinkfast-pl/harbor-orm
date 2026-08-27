// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(table = "audited_entities")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AuditedEntity {

    @Id
    @SequenceGenerated(sequence = "audited_entities_id_seq")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "insert_counter", nullable = false)
    private Integer insertCounter;

    @Column(name = "update_counter", nullable = false)
    private Integer updateCounter;

    // Transient fields for @Post callback tracking (not persisted)
    private transient int postInsertCounter;
    private transient int postUpdateCounter;
    private transient boolean postDeleteCalled;
    private transient boolean preDeleteCalled;

    @PreInsert
    void onPreInsert() {
        this.createdAt = LocalDateTime.now();
        this.insertCounter = this.insertCounter == null ? 1 : this.insertCounter + 1;
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updateCounter = this.updateCounter == null ? 1 : this.updateCounter + 1;
    }

    @PreDelete
    void onPreDelete() {
        this.preDeleteCalled = true;
    }

    @PostInsert
    void onPostInsert() {
        this.postInsertCounter++;
    }

    @PostUpdate
    void onPostUpdate() {
        this.postUpdateCounter++;
    }

    @PostDelete
    void onPostDelete() {
        this.postDeleteCalled = true;
    }

    /**
     * Convenience constructor for creating new audited entities.
     */
    public AuditedEntity(String name) {
        this.id = null;
        this.name = name;
        this.createdAt = null;
        this.updatedAt = null;
        this.insertCounter = 0;
        this.updateCounter = 0;
    }
}
