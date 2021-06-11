/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Peter Gromov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.tools;

import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A map containing multiple values per key, memory-optimized for case when there's only one value.
 */
public class MostlySingularMultiMap<K, V> {
  private final THashMap<K, Object> map;

  public MostlySingularMultiMap(Map<K, List<V>> contents) {
    map = new THashMap<>(contents.size());
    for (Map.Entry<K, List<V>> entry : contents.entrySet()) {
      List<V> value = entry.getValue();
      map.put(entry.getKey(), value.size() == 1 ? value.get(0) : value.toArray());
    }
    map.trimToSize();
  }

  @Nullable
  public List<V> getList(K key) {
    Object o = map.get(key);
    //noinspection unchecked
    return o == null ? null :
           o instanceof Object[] ? Arrays.asList((V[]) o) :
           Collections.singletonList((V) o);
  }


}
