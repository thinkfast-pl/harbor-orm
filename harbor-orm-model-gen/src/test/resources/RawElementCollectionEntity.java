// SPDX-License-Identifier: Apache-2.0

package entities;

import io.github.thinkfastpl.harbororm.api.annotations.*;
import lombok.Getter;

import java.util.List;

@Entity(table = "raw_element_collection")
@Getter
class RawElementCollectionEntity {

    @Id
    private Long id;

    @ElementCollection(
            table = "raw_tags",
            joinColumns = @JoinColumn(
                    name = "entity_id",
                    fieldType = Long.class
            )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List tags;
}
