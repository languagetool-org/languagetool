/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Pedro Goulart
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
package org.languagetool.rules.pt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BrazilianToponymMap {
  private final Map<String, List<String>> toponymMap;
  private static final BrazilianStateInfoMap stateMap = new BrazilianStateInfoMap();

  BrazilianToponymMap() {
    toponymMap = new BrazilianToponymMapLoader().buildMap();
  }

  // Since the actually toponym string is only a heuristic, it could be that we match more than we need, e.g.:
  // "Venho do Rio de Janeiro" will match, hungrily, the whole thing, rather than just "Rio de Janeiro".
  // To account for this, we loop, dropping the leftmost element of the toponym until we can't check any more.
  private <T> T toponymIter(String toponym, Function<String, T> processor, T defaultValue) {
    String normalisedToponym = toponym.replace('-', ' ').toLowerCase();
    String[] toponymParts = normalisedToponym.split(" ");
    int toponymLength = toponymParts.length;
    for (int i = 0; i < toponymLength; i++) {
      String toponymToCheck = String.join(" ", Arrays.copyOfRange(toponymParts, i, toponymLength));
      T result = processor.apply(toponymToCheck);
      if (result instanceof Boolean) {
        if ((Boolean) result) {
          return result;
        }
      } else if (result != null) {
        return result;
      }
    }
    return defaultValue;
  }

  public boolean isValidToponym(String toponym) {
    return toponymIter(toponym, toponymToCheck ->
        toponymMap.values().stream().anyMatch(list -> list.contains(toponymToCheck)) ? true : null,
      false);
  }

  public boolean isToponymInState(String toponym, String state) {
    List<String> municipalities = toponymMap.get(state);
    if (municipalities == null) {
      return false;
    }
    Function<String, Boolean> processor = municipalities::contains;
    return toponymIter(toponym, processor, false);
  }

  public BrazilianToponymStateCheckResult getStatesWithMunicipality(String toponym) {
    List<BrazilianStateInfo> allMatchedStates = new ArrayList<>();
    AtomicReference<String> matchedToponym = new AtomicReference<>(null);
    String[] originalToponymParts = toponym.split(" ");
    String[] normalizedToponymParts = toponym.replace('-', ' ').toLowerCase().split(" ");
    for (int i = 0; i < normalizedToponymParts.length; i++) {
      String normalizedToponymToCheck = String.join(" ",
        Arrays.copyOfRange(normalizedToponymParts, i, normalizedToponymParts.length));
      for (Map.Entry<String, List<String>> entry : toponymMap.entrySet()) {
        if (entry.getValue().contains(normalizedToponymToCheck)) {
          allMatchedStates.add(stateMap.get(entry.getKey()));
          if (matchedToponym.get() == null) {
            String prettyToponym = String.join(" ",
              Arrays.copyOfRange(originalToponymParts, i, originalToponymParts.length));
            matchedToponym.set(prettyToponym);
          }
        }
      }
    }
    return new BrazilianToponymStateCheckResult(allMatchedStates, matchedToponym.get());
  }
}
