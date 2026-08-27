// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import java.util.Set;

interface LazyList {

    boolean isPotentiallyModified();

    Set<Object> getMissingEntities();
}
