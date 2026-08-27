// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.interpreter;

import io.github.thinkfastpl.harbororm.modelgen.metadata.*;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public record SourceInterpreterResult(
        @NonNull List<EntityMetadata> entities,
        @NonNull List<EmbeddableMetadata> embeddableList
) {

    public List<TableMetadata> extractTables(@NonNull String tablesPackage) {
        final List<TableMetadata> tables = new ArrayList<>();
        final Set<String> generatedJoinTables = new HashSet<>();

        final Map<String, List<JoinColumnMetadata>> relationJoinColumnsByTargetEntity = new HashMap<>();
        for (EntityMetadata entity : entities) {
            for (EntityRelationMetadata relation : entity.entityRelations()) {
                relationJoinColumnsByTargetEntity
                        .computeIfAbsent(relation.getEntityType().getFullName(), k -> new ArrayList<>())
                        .addAll(relation.getJoinColumns());
            }
        }

        for (EntityMetadata entity : entities) {
            final List<ColumnMetadata> entityColumns = new ArrayList<>(entity.columns());

            for (EmbeddedMetadata embeddedMetadata : entity.embeddedAttributes()) {
                entityColumns.addAll(getFromEmbedded(embeddedMetadata));
            }

            for (JoinColumnMetadata joinColumn : relationJoinColumnsByTargetEntity
                    .getOrDefault(entity.packageName() + "." + entity.className(), List.of())) {
                boolean alreadyPresent = entityColumns.stream()
                        .anyMatch(c -> Objects.equals(c.columnName(), joinColumn.name()));

                if (!alreadyPresent) {
                    entityColumns.add(joinColumn.toSimpleColumnMetadata());
                }
            }

            tables.add(new TableMetadata(
                    tablesPackage,
                    entity.tableName(),
                    entity.schemaName(),
                    entityColumns
            ));

            for (ElementCollectionMetadata elementCollection : entity.elementCollections()) {
                final List<ColumnMetadata> columnMetadataList = elementCollection.joinColumns().stream()
                        .map(joinColumn -> {
                            ColumnMetadata referencedColumnMetadata = entity.columns().stream()
                                    .filter(c -> Objects.equals(c.columnName(), joinColumn.referencedColumnName()))
                                    .findFirst()
                                    .or(() -> entity.embeddedAttributes().stream()
                                            .flatMap(e -> getFromEmbedded(e).stream())
                                            .filter(c -> Objects.equals(c.columnName(), joinColumn.referencedColumnName()))
                                            .findFirst())
                                    .orElseGet(() -> entity.columns().stream()
                                            .filter(ColumnMetadata::id)
                                            .findFirst()
                                            .orElseThrow(() -> new IllegalStateException("Entity does not contain referenced column or id column not found"))
                                    );

                            return joinColumn.toColumnMetadata(referencedColumnMetadata);
                        })
                        .collect(Collectors.toCollection(ArrayList::new));

                if (elementCollection.columnMetadata() != null) {
                    columnMetadataList.add(elementCollection.columnMetadata().assureFieldName());
                }

                if (elementCollection.embeddedMetadata() != null) {
                    columnMetadataList.addAll(getFromEmbedded(elementCollection.embeddedMetadata()));
                }

                tables.add(new TableMetadata(
                        tablesPackage,
                        elementCollection.tableName(),
                        elementCollection.tableSchemaName(),
                        columnMetadataList
                ));
            }

            for (ManyToManyMetadata manyToMany : entity.manyToManyRelations()) {
                String tableKey = (manyToMany.tableSchemaName() != null ? manyToMany.tableSchemaName() + "." : "")
                        + manyToMany.tableName();

                if (!generatedJoinTables.add(tableKey)) {
                    continue;
                }

                final List<ColumnMetadata> columnMetadataList = new ArrayList<>();
                manyToMany.joinColumns().stream()
                        .map(JoinColumnMetadata::toSimpleColumnMetadata)
                        .forEach(columnMetadataList::add);
                manyToMany.inverseJoinColumns().stream()
                        .map(JoinColumnMetadata::toSimpleColumnMetadata)
                        .forEach(columnMetadataList::add);

                tables.add(new TableMetadata(
                        tablesPackage,
                        manyToMany.tableName(),
                        manyToMany.tableSchemaName(),
                        columnMetadataList
                ));
            }
        }

        return tables;
    }

    private List<ColumnMetadata> getFromEmbedded(@NonNull EmbeddedMetadata embeddedMetadata) {
        return embeddedMetadata.getColumnsForQTable();
    }
}
