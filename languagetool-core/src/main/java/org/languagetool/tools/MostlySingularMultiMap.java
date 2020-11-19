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
  private final Map<K, Object> map;

  public MostlySingularMultiMap(Map<K, List<V>> contents) {
    map = new THashMap<>(contents.size());
    for (Map.Entry<K, List<V>> entry : contents.entrySet()) {
      List<V> value = entry.getValue();
      map.put(entry.getKey(), value.size() == 1 ? value.get(0) : value.toArray());
    }
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
