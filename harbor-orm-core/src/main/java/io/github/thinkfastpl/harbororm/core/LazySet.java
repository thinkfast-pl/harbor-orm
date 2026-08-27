// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import java.util.Set;

interface LazySet {

    boolean isPotentiallyModified();

    Set<Object> getAddedElements();

    Set<Object> getRemovedElements();
}
