// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.metadata.*;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Getter
class AttributeExtractor {
    private final List<QColumn<?>> columns = new ArrayList<>();
    private List<QColumn<?>> allColumnsRecursiveCache;
    private final List<QEmbeddable<?>> embeddables;
    private final List<QElementCollection<?>> elementCollections;
    private final List<QEntityRelation<?, ?>> entityRelations;
    private final List<QManyToMany<?>> manyToManyRelations;

    AttributeExtractor(@NonNull QAttributeHolder attributeHolder) {
        List<QEmbeddable<?>> embeddables = null;
        List<QElementCollection<?>> elementCollections = null;
        List<QEntityRelation<?, ?>> entityRelations = null;
        List<QManyToMany<?>> manyToManyRelations = null;

        for (QAttribute attribute : attributeHolder.getAllAttributes()) {
            if (attribute instanceof QColumn<?> column) {
                columns.add(column);
            } else if (attribute instanceof QEmbeddable<?> embeddable) {
                if (embeddables == null) {
                    embeddables = new ArrayList<>();
                }
                embeddables.add(embeddable);
            } else if (attribute instanceof QElementCollection<?> elementCollection) {
                if (elementCollections == null) {
                    elementCollections = new ArrayList<>();
                }
                elementCollections.add(elementCollection);
            } else if (attribute instanceof QEntityRelation<?, ?> entityRelation) {
                if (entityRelations == null) {
                    entityRelations = new ArrayList<>();
                }
                entityRelations.add(entityRelation);
            } else if (attribute instanceof QManyToMany<?> manyToMany) {
                if (manyToManyRelations == null) {
                    manyToManyRelations = new ArrayList<>();
                }
                manyToManyRelations.add(manyToMany);
            }
        }

        this.embeddables = embeddables != null ? embeddables : Collections.emptyList();
        this.elementCollections = elementCollections != null ? elementCollections : Collections.emptyList();
        this.entityRelations = entityRelations != null ? entityRelations : Collections.emptyList();
        this.manyToManyRelations = manyToManyRelations != null ? manyToManyRelations : Collections.emptyList();
    }

    public List<QColumn<?>> getAllColumnsRecursive() {
        if (allColumnsRecursiveCache != null) {
            return allColumnsRecursiveCache;
        }
        allColumnsRecursiveCache = Stream.concat(
                columns.stream(),
                embeddables.stream().flatMap(e -> new AttributeExtractor(e).getAllColumnsRecursive().stream())
        ).toList();
        return allColumnsRecursiveCache;
    }
}
